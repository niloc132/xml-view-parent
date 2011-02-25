/**
 *  Copyright 2011 Colin Alworth
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.colinalworth.xmlview.rebind;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.colinalworth.xmlview.client.schema.Schema;
import com.colinalworth.xmlview.client.schema.Schema.SchemaPath;
import com.colinalworth.xmlview.client.schema.Schema.SchemaURL;
import com.colinalworth.xmlview.client.schema.impl.AbstractSchemaImpl;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Name;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * 
 * @todo this needs support for namespaces, even though IE will not support them...
 * @todo check attr value, content types
 * @todo consider an approach to check full xpaths?
 * 
 * @author colin
 *
 */
public class XmlValidatorGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		// validate the interface, annotations
		TypeOracle oracle = context.getTypeOracle();
		JClassType toGenerate = oracle.findType(typeName).isInterface();
		if (toGenerate == null) {
			logger.log(Type.ERROR, typeName + " is not an interface type");
			throw new UnableToCompleteException();
		}
		JClassType schema = oracle.findType(Name.getSourceNameForClass(Schema.class)).isInterface();
		if (!schema.isAssignableFrom(toGenerate)) {
			// this shouldn't be possible unless the generator is deliberately invoked
			logger.log(Type.ERROR, "Declared interface must be assignable from XmlValidator");
			throw new UnableToCompleteException();
		}

		SchemaURL urlAnnotation = toGenerate.getAnnotation(SchemaURL.class);
		SchemaPath pathAnnotation = toGenerate.getAnnotation(SchemaPath.class);
		// get a handle on the file that has the schema
		final Sources sources = new Sources();
		if (urlAnnotation != null) {
			for (String url : urlAnnotation.value()) {
				try {
					sources.urls.add(new URL(url));
				} catch (MalformedURLException e) {
					logger.log(Type.ERROR, "Problem with @SchemaURL(\"" + url + "\")", e);
					throw new UnableToCompleteException();
				}
			}
		}
		if (pathAnnotation != null) {
			for (String file : pathAnnotation.value()) {
				sources.files.add(new File(file));
			}
		}
		if (urlAnnotation == null && pathAnnotation == null){
			assert urlAnnotation == null && pathAnnotation == null;
			logger.log(Type.ERROR, "A Path or URL must be defined");
			throw new UnableToCompleteException();
		}

		// make the impl class
		String packageName = toGenerate.getPackage().getName();
		String simpleSourceName = toGenerate.getName().replace('.', '_') + "_Impl";
		PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
		if (pw == null) {
			return packageName + "." + simpleSourceName;
		}

		//public class X implements X {
		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleSourceName);
		factory.setSuperclass(Name.getSourceNameForClass(AbstractSchemaImpl.class));
		factory.addImplementedInterface(typeName);

		//factory.addImport(Name.getSourceNameForClass(GWT.class));
		factory.addImport(Name.getSourceNameForClass(Node.class));
		factory.addImport(Name.getSourceNameForClass(Element.class));
		factory.addImport(Name.getSourceNameForClass(Attr.class));
		factory.addImport(Name.getSourceNameForClass(CharacterData.class));
		factory.addImport(Name.getSourceNameForClass(JsArray.class));


		SourceWriter sw = factory.createSourceWriter(context, pw);

		// generate the class
		ValidatorCreator c = new ValidatorCreator(simpleSourceName, sources, context, logger, sw);

		c.generateCtor();
		c.generateMethods();

		sw.commit(logger);
		return packageName + "." + simpleSourceName;
	}
	private static class Sources {
		List<URL> urls = new ArrayList<URL>();
		List<File> files = new ArrayList<File>();
	}

	private static class ValidatorCreator {
		private final String simpleSourceName;
		//private final XSOMParser parser;

		private final TreeLogger logger;
		private final GeneratorContext context;
		private final SourceWriter sw;

		//private final XSSchemaSet schema;

		//private List<XSElementDecl> elements;
		private Map<NamespacedElement, Set<XSElementDecl>> elementChildren;
		private Map<NamespacedElement, Set<XSAttributeDecl>> elementAttributes;
		/**
		 * 
		 */
		public ValidatorCreator(String simpleSourceName, Sources sources, GeneratorContext context, 
				TreeLogger logger, SourceWriter sw) throws UnableToCompleteException {
			this.logger = logger.branch(Type.INFO, "Creating validator impl for " + simpleSourceName);
			this.simpleSourceName = simpleSourceName;
			this.sw = sw;
			this.context = context;
			this.elementChildren = new HashMap<NamespacedElement, Set<XSElementDecl>>();
			this.elementAttributes = new HashMap<NamespacedElement, Set<XSAttributeDecl>>();

			try {
				final TreeLogger parsingLogger = logger.branch(Type.INFO, "Reading schemas");
				XSOMParser parser = new XSOMParser();
				parser.setErrorHandler(new ErrorHandler() {
					@Override
					public void warning(SAXParseException exception) throws SAXException {
						log(Type.WARN, "Warning occured during parse", exception);
					}
					@Override
					public void fatalError(SAXParseException exception) throws SAXException {
						log(Type.ERROR, "Fatal error occured during parse", exception);
					}
					@Override
					public void error(SAXParseException exception) throws SAXException {
						log(Type.ERROR, "Error occured during parse", exception);
					}
					private void log(Type type, String prefix, SAXParseException ex) {
						parsingLogger.log(type, String.format("%1$s (%2$s at line %3$s, col %4$s)", prefix, ex.getPublicId(), ex.getLineNumber(), ex.getColumnNumber()));
					}
				});
				for (File schema : sources.files) {
					parser.parse(schema);
				}
				for (URL schema : sources.urls) {
					parser.parse(schema);
				}

				XSSchemaSet schemas = parser.getResult();

				if (schemas == null) {
					throw new UnableToCompleteException();
				}
				LinkedList<XSElementDecl> elements = new LinkedList<XSElementDecl>();
				Iterator<XSElementDecl> iter = schemas.iterateElementDecls();
				while (iter.hasNext()) {
					elements.add(iter.next());
				}


				while (!elements.isEmpty()) {
					NamespacedElement elt = new NamespacedElement(elements.removeFirst());
					TreeLogger l = parsingLogger.branch(Type.INFO, elt.getElement().getName());
					Set<XSElementDecl> subElts = elementChildren.get(elt);
					Set<XSAttributeDecl> attrs = elementAttributes.get(elt);
					if (!elementChildren.containsKey(elt)) {
						assert elementAttributes.containsKey(elt) == false : "Inconsistent attr/children maps";
						elementChildren.put(elt, subElts = new HashSet<XSElementDecl>());
						elementAttributes.put(elt, attrs = new HashSet<XSAttributeDecl>());
					}

					if (elt.getElement().getType().isSimpleType()) {
						l.log(Type.INFO, elt.getElement().getType().getName());
					} else {
						XSComplexType complex = elt.getElement().getType().asComplexType();
						for (XSAttributeUse attr : complex.getAttributeUses()) {
							l.log(Type.INFO, attr.getDecl().getName());
							attrs.add(attr.getDecl());
						}
						XSParticle content = complex.getContentType().asParticle();
						if (content != null && content.getTerm().isModelGroup()) {
							LinkedList<XSModelGroup> groups = new LinkedList<XSModelGroup>();
							groups.add(content.getTerm().asModelGroup());
							while (!groups.isEmpty()) {
								for (XSParticle p : groups.removeFirst().getChildren()){
									XSTerm pterm = p.getTerm();
									if (pterm.isElementDecl()){ //xs:element inside complex type
										// check to see if we know about this element decl
										// this check is needed to be sure we don't look for children
										// twice
										if (!subElts.contains(pterm.asElementDecl())) {
											l.log(Type.INFO, pterm.asElementDecl().getName());
											elements.add(pterm.asElementDecl());
											subElts.add(pterm.asElementDecl());
										}
									} else if (pterm.isModelGroup()) {
										groups.add(pterm.asModelGroup());
									}
								}
							}
						}
					}
				}

			} catch (SAXException e) {
				logger.log(Type.ERROR, "Parse Exception occured reading schema", e);
				throw new UnableToCompleteException();
			} catch (IOException e) {
				logger.log(Type.ERROR, "IO Exception occured reading schema", e);
				throw new UnableToCompleteException();
			}


		}
		/**
		 * 
		 */
		public void generateCtor() throws UnableToCompleteException {
			TreeLogger l = logger.branch(Type.INFO, "Creating constructor");
			sw.println("public %1$s() {", simpleSourceName);
			sw.indent();
			sw.println("super(getAttrs(), getElts());");
			sw.outdent();
			sw.println("}");
		}

		/**
		 * 
		 */
		public void generateMethods() throws UnableToCompleteException {
			TreeLogger l = logger.branch(Type.INFO, "Creating data creator methods");

			sw.println("static native JsArray getAttrs() /*-{");
			sw.indent();
			sw.println("return %1$s;", generateElementAttributeMap());
			sw.outdent();
			sw.println("}-*/;");

			sw.println("static native JsArray getElts() /*-{");
			sw.indent();
			sw.println("return %1$s;", generateElementChildMap());
			sw.outdent();
			sw.println("}-*/;");
		}

		private String generateElementChildMap() {
			//first layer is namespace, second is element, last is (child):true (or not)
			JSONObject namespaces = new JSONObject();


			try {
				for (NamespacedElement parent : this.elementChildren.keySet()) {
					if (namespaces.isNull(parent.getElement().getTargetNamespace())) {
						namespaces.put(parent.getElement().getTargetNamespace(), new JSONObject());
					}
					JSONObject namespace = namespaces.getJSONObject(parent.getElement().getTargetNamespace());

					JSONArray inner = new JSONArray();
					for (XSElementDecl child : this.elementChildren.get(parent)) {
						JSONObject entry = new JSONObject();
						entry.put("n", child.getName());
						entry.put("u", child.getTargetNamespace());
						inner.put(entry);
					}
					namespace.put(parent.getElement().getName(), inner);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return namespaces.toString();
		}
		private String generateElementAttributeMap() {
			//first layout is namespace, second is element, last is attr:true (or not)
			JSONObject namespaces = new JSONObject();

			try {
				for (NamespacedElement parent : this.elementAttributes.keySet()) {
					if (namespaces.isNull(parent.getElement().getTargetNamespace())) {
						namespaces.put(parent.getElement().getTargetNamespace(), new JSONObject());
					}
					JSONObject namespace = namespaces.getJSONObject(parent.getElement().getTargetNamespace());

					JSONObject inner = new JSONObject();
					for (XSAttributeDecl child : this.elementAttributes.get(parent)) {
						inner.put(child.getName(), true);
					}
					namespace.put(parent.getIdentifier(), inner);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return namespaces.toString();
		}


		private String generateElementsWithNonElementContents() {
			//single layer (for now), just element:true if allowed to have simple content
			JSONObject map = new JSONObject();

			try {
				for (NamespacedElement elt : this.elementAttributes.keySet()) {
					if (elt.getElement().getType().isSimpleType()) {
						map.put(elt.getIdentifier(), true);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return map.toString();
		}
	}
	public final static class NamespacedElement {
		private final XSElementDecl elt;
		public NamespacedElement(XSElementDecl elt) {
			this.elt = elt;
		}
		public XSElementDecl getElement() {
			return elt;
		}
		public String getIdentifier() {
			return elt.getTargetNamespace() + ":" + elt.getName();
		}

		@Override
		public int hashCode() {
			return elt.getTargetNamespace().hashCode() ^ elt.getName().hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NamespacedElement) {
				NamespacedElement that = (NamespacedElement) obj;
				return this.elt.getTargetNamespace().equals(that.elt.getTargetNamespace()) && this.elt.getName().equals(that.elt.getName());
			}
			return false;
		}
	}
}
