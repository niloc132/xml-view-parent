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
package com.colinalworth.xmlview.client.schema.impl;

import java.util.HashSet;
import java.util.Set;

import com.colinalworth.xmlview.client.schema.Schema;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.xml.client.Node;


/**
 * @author colin
 * 
 */
public abstract class AbstractSchemaImpl implements Schema {
	private final NamespacedElementWrapperMap attrs;
	private final NamespacedElementWrapperMap elts;
	/**
	 * 
	 */
	public AbstractSchemaImpl(JsArray<NamespacedNodeJSO> attrs, JsArray<NamespacedNodeJSO> elts) {
		this.attrs = attrs.cast();
		this.elts = elts.cast();
	}

	static final class NamespacedElementWrapperMap extends JavaScriptObject {
		protected NamespacedElementWrapperMap() {
			// TODO Auto-generated constructor stub
		}
		native JsArray<NamespacedNodeJSO> get(String namespace, String elt) /*-{
			return this[namespace][elt];
		}-*/;
	}
	static final class NamespacedNodeJSO extends JavaScriptObject implements NamespacedNode {
		protected NamespacedNodeJSO() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public native String getNamespaceURI() /*-{
			return this.u;
		}-*/;
		@Override
		public native String getNodeName() /*-{
			return this.n;
		}-*/;
	}



	@Override
	public NamespacedNode createNode(Node node) {
		String[] nameParts = node.getNodeName().split(":");
		return createNode(node.getNamespaceURI(), nameParts[nameParts.length - 1]);
	}

	@Override
	public native NamespacedNode createNode(String uri, String name) /*-{
		return {u:uri,n:name};
	}-*/;
	@Override
	public Set<NamespacedNode> getAttributes(String namespace, String elementName) {
		return wrap(attrs.get(namespace, elementName));
	}

	@Override
	public Set<NamespacedNode> getChildElements(String namespace, String elementName) {
		return wrap(elts.get(namespace, elementName));
	}

	@Override
	public Set<NamespacedNode> getRootNodes() {
		return null;
	}
	@Override
	public Set<NamespacedNode> getAttributes(NamespacedNode element) {
		return getAttributes(element.getNamespaceURI(), element.getNodeName());
	}

	@Override
	public Set<NamespacedNode> getChildElements(NamespacedNode element) {
		return getChildElements(element.getNamespaceURI(), element.getNodeName());
	}

	/**
	 * @param jsArray
	 * @return
	 */
	private Set<Schema.NamespacedNode> wrap(JsArray<NamespacedNodeJSO> jsArray) {
		Set<Schema.NamespacedNode> nodes = new HashSet<Schema.NamespacedNode>();
		for (int i = 0; i < jsArray.length(); i++) {
			nodes.add(jsArray.get(i));
		}

		return nodes;
	}
}
