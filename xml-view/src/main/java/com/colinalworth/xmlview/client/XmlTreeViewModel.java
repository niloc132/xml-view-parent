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
package com.colinalworth.xmlview.client;

import java.util.ArrayList;
import java.util.List;

import com.colinalworth.xmlview.client.validator.AcceptAllXmlValidator;
import com.colinalworth.xmlview.client.validator.XmlValidator;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Comment;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;

/**
 * @author colin
 *
 */
public class XmlTreeViewModel implements TreeViewModel {

	private final XmlValidator validator;
	private final ElementCell nodeCell;

	/**
	 * 
	 */
	public XmlTreeViewModel() {
		this(new AcceptAllXmlValidator());
	}
	public XmlTreeViewModel(XmlValidator validator) {
		this.validator = validator;

		nodeCell = new ElementCell();
	}
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		final Node node = (Node)value;
		final ListDataProvider<Node> dataProvider = new ListDataProvider<Node>(getChildren(node));
		ValueUpdater<Node> parentUpdater = new ValueUpdater<Node>() {
			@Override
			public void update(Node value) {
				dataProvider.setList(getChildren(node));
				//dataProvider.refresh();//shouldn't be necessary...
			}
		};
		DefaultNodeInfo<Node> nodeInfo = new DefaultNodeInfo<Node>(dataProvider, nodeCell, null, parentUpdater);
		return nodeInfo;
	}

	@Override
	public boolean isLeaf(Object value) {
		Node node = (Node)value;
		if (node instanceof Document) {
			//TODO consider replacing this with return true
			return !node.hasChildNodes();
		} else if (node instanceof Element) {
			//is not a leaf if it has children, or if it has attributes
			return !node.hasChildNodes() && !node.hasAttributes();
		}
		//if not a Node, it is a leaf
		return true;
	}

	private List<Node> getChildren(Node node) {
		List<Node> list = new ArrayList<Node>();

		if (node instanceof Element) {
			for (int i = 0; i < node.getAttributes().getLength(); i++) {
				list.add(WrappedAttr.wrap((Attr)node.getAttributes().item(i), (Element)node));
			}
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			list.add(node.getChildNodes().item(i));
		}

		return list;
	}
	private static class ViewState {
		enum Section { 
			TagName, AttributeName, AttributeValue, Content, None;
			public static Section getViewState(com.google.gwt.dom.client.Element elt, Node node) {
				if (node instanceof Element) {
					return Section.TagName;
				} else if (node instanceof Attr) {
					return elt.getClassName().contains("attrName") ? Section.AttributeName: Section.AttributeValue;
				} else if (node instanceof CharacterData) {
					return Section.Content;
				}
				return Section.None;
			}
		}
		public Section section;
		//public String sectionName;

		public String value;
	}
	interface Template extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span>&lt;<input type=\"text\" value=\"{0}\" />&gt;</span>")
		SafeHtml element(String nodeName);
		@SafeHtmlTemplates.Template("<span><input class=\"attrName\" type=\"text\" value=\"{0}\" /> = <input class=\"attrValue\" type=\"text\" value=\"{1}\" /></span>")
		SafeHtml attribute(String name, String value);
		@SafeHtmlTemplates.Template("<span>#text <textarea>{0}</textarea></span>")
		SafeHtml text(String contents);
		@SafeHtmlTemplates.Template("<span>#cdata <textarea>{0}</textarea></span>")
		SafeHtml cdata(String contents);
		@SafeHtmlTemplates.Template("&lt;--{0}--&gt;")
		SafeHtml comment(String contents);
	}
	private static Template template;
	public class ElementCell extends AbstractEditableCell<Node, ViewState> {

		private Object lastKey;


		public ElementCell() {
			super("blur", "focus", "keydown", "keyup");
			if (template == null) {
				template = GWT.create(Template.class);
			}
		}
		@Override
		public void render(Cell.Context context, Node value, SafeHtmlBuilder sb) {
			Object key = context.getKey();
			ViewState viewData = getViewData(key);
			if (viewData != null && viewData.section != ViewState.Section.None &&
					viewData.value != null && viewDataMatchesValue(viewData, value)) {
				clearViewData(key);
				viewData = null;
			}

			if (value instanceof Element) {
				sb.append(template.element(value.getNodeName()));
			} else if (value instanceof CDATASection) {
				sb.append(template.cdata(value.getNodeValue()));
			} else if (value instanceof Text) {
				sb.append(template.text(value.getNodeValue()));
			} else if (value instanceof Attr) {
				sb.append(template.attribute(((Attr)value).getName(), value.getNodeValue()));
			} else if (value instanceof Comment) {
				sb.append(template.comment(((Comment)value).getData()));
			}
		}


		/**
		 * @param viewData
		 * @param value
		 * @return
		 */
		private boolean viewDataMatchesValue(ViewState viewData, Node value) {
			switch (viewData.section) {
			case TagName:
				assert value instanceof Element;
				return viewData.value.equals(value.getNodeName());
			case AttributeName:
				assert value instanceof Attr;
				return viewData.value.equals(((Attr)value).getName());
			case AttributeValue:
				assert value instanceof Attr;
				return viewData.value.equals(value.getNodeValue());
			case Content:
				assert value instanceof CharacterData;
				return viewData.value.equals(value.getNodeValue());
			case None:
			default:
			}
			return false;
		}

		@Override
		public boolean isEditing(Cell.Context context, com.google.gwt.dom.client.Element parent, Node value) {
			return lastKey != null && lastKey.equals(context.getKey());
		}

		@Override
		public boolean resetFocus(Cell.Context context, com.google.gwt.dom.client.Element parent, Node value) {
			if (isEditing(context, parent, value)) {
				//TODO focus correct element
				return true;
			}

			return false;
		}

		@Override
		public void onBrowserEvent(Cell.Context context,
				com.google.gwt.dom.client.Element parent, Node value,
				NativeEvent event, ValueUpdater<Node> valueUpdater) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
			com.google.gwt.dom.client.Element target = event.getEventTarget().cast();

			// Ignore events that don't target the input.
			if (!"INPUT".equals(target.getTagName()) && !target.getTagName().equals("TEXTAREA")) {
				return;
			}

			String eventType = event.getType();
			if ("keyup".equals(eventType)) {
				updateViewState(context.getKey(), value, target);
			} else if ("focus".equals(eventType)) {

				lastKey = context.getKey();
			} else if ("blur".equals(eventType)) {
				finishEdit(value, target);
				valueUpdater.update(value);
				lastKey = null;
			}

		}


		@Override
		protected void onEnterKeyDown(Cell.Context context,
				com.google.gwt.dom.client.Element parent, Node value,
				NativeEvent event, ValueUpdater<Node> valueUpdater) {
			//if the event is directed at an input, finish editing, otherwise focus on first elt
			com.google.gwt.dom.client.Element target = event.getEventTarget().cast();
			if (target.getTagName().equals("INPUT") || target.getTagName().equals("SELECT") || target.getTagName().equals("TEXTAREA")) {
				finishEdit(value, target);
				// TODO move focus to next field (if attr name, then value, if has children, 
				// then first child, if has next sibling, then it, if parent has sibling, then it, 
				// recursively)
			} else {
				this.lastKey = context.getKey();
				parent.getFirstChildElement().focus();
			}
		}
		/**
		 * @param context
		 * @param value
		 */
		private void finishEdit(Node value, com.google.gwt.dom.client.Element target) {
			ViewState state = updateViewState(lastKey, value, target);
			String newValue = target.<InputElement>cast().getValue();
			boolean valid = true;
			Element parent = (Element)value.getParentNode();
			switch (state.section) {
			case AttributeName:
				Attr attr = (Attr) value;
				//TODO this might lose namespace data
				parent.removeAttribute(attr.getName());
				parent.setAttribute(newValue, attr.getValue());


				valid = validator.isAttributeNameValid(parent.getAttributeNode(attr.getName()));
				break;
			case AttributeValue:
				value.setNodeValue(newValue);
				valid = validator.isAttributeValueValid((Attr) value);
				break;
			case Content:
				((CharacterData)value).setData(newValue);
				valid = validator.isContentsValid((CharacterData) value);
				break;
			case TagName:
				Element elt = (Element) value;
				Element replacement = elt.getOwnerDocument().createElement(newValue);
				while (elt.getChildNodes().getLength() != 0) {
					replacement.appendChild(elt.getChildNodes().item(0));
				}
				//TODO this might lose namespace data
				for (int i = 0; i < elt.getAttributes().getLength(); i++) {
					Attr a = (Attr)elt.getAttributes().item(i);
					replacement.setAttribute(a.getName(), a.getValue());
				}

				parent.replaceChild(replacement, elt);

				valid = validator.isElementNameValid(replacement);
			}
			if (!valid) {
				Window.alert("Seems to be invalid: " + newValue + " in " + parent.getNodeName());
				//TODO mark invalid
			}
			this.lastKey = null;
			target.blur();
		}
		private ViewState updateViewState(Object key, Node value,
				com.google.gwt.dom.client.Element target) {
			ViewState viewState = getViewData(key);
			if (viewState == null) {
				viewState = new ViewState();
				setViewData(key, viewState);
			}
			viewState.section = ViewState.Section.getViewState(target, value);
			viewState.value = target.<InputElement>cast().getValue();
			return viewState;
		}
	}

	private static class WrappedAttr implements Attr {
		private final Attr attr;
		private final Element elt;
		public static WrappedAttr wrap(Attr attr, Element elt) {
			if (attr instanceof WrappedAttr) {
				return (WrappedAttr) attr;
			}
			return new WrappedAttr(attr, elt);
		}
		private WrappedAttr(Attr attr, Element elt) {
			this.attr = attr;
			this.elt = elt;
		}
		@Override
		public Node appendChild(Node newChild) {
			return attr.appendChild(newChild);
		}

		@Override
		public Node cloneNode(boolean deep) {
			return attr.cloneNode(deep);
		}

		@Override
		public NamedNodeMap getAttributes() {
			return attr.getAttributes();
		}

		@Override
		public NodeList getChildNodes() {
			return attr.getChildNodes();
		}

		@Override
		public Node getFirstChild() {
			return attr.getFirstChild();
		}

		@Override
		public Node getLastChild() {
			return attr.getLastChild();
		}

		@Override
		public String getNamespaceURI() {
			return attr.getNamespaceURI();
		}

		@Override
		public Node getNextSibling() {
			return attr.getNextSibling();
		}

		@Override
		public String getNodeName() {
			return attr.getNodeName();
		}

		@Override
		public short getNodeType() {
			return attr.getNodeType();
		}

		@Override
		public String getNodeValue() {
			return attr.getNodeValue();
		}

		@Override
		public Document getOwnerDocument() {
			return attr.getOwnerDocument();
		}

		@Override
		public Node getParentNode() {
			return elt;
		}

		@Override
		public String getPrefix() {
			return attr.getPrefix();
		}

		@Override
		public Node getPreviousSibling() {
			return attr.getPreviousSibling();
		}

		@Override
		public boolean hasAttributes() {
			return attr.hasAttributes();
		}

		@Override
		public boolean hasChildNodes() {
			return attr.hasChildNodes();
		}

		@Override
		public Node insertBefore(Node newChild, Node refChild) {
			return attr.insertBefore(newChild, refChild);
		}

		@Override
		public void normalize() {
			attr.normalize();
		}

		@Override
		public Node removeChild(Node oldChild) {
			return attr.removeChild(oldChild);
		}

		@Override
		public Node replaceChild(Node newChild, Node oldChild) {
			return attr.replaceChild(newChild, oldChild);
		}

		@Override
		public void setNodeValue(String nodeValue) {
			attr.setNodeValue(nodeValue);
		}

		@Override
		public String getName() {
			return attr.getName();
		}

		@Override
		public boolean getSpecified() {
			return attr.getSpecified();
		}

		@Override
		public String getValue() {
			return attr.getValue();
		}

	}
}
