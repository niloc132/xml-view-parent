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
package com.colinalworth.xmlview.client.tree;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Text;

/**
 * @author colin
 *
 */
public class XmlTreeViewModel implements TreeViewModel {
	private Cell<Node> nodeCell;


	/**
	 * 
	 */
	public XmlTreeViewModel() {
		List<HasCell<Node, ?>> cells = new ArrayList<HasCell<Node,?>>();
		HasCell<Node, String> tagName = new HasCell<Node, String>() {

			@Override
			public String getValue(Node object) {
				return object.getNodeName();
			}

			@Override
			public FieldUpdater<Node, String> getFieldUpdater() {
				return null;
			}

			@Override
			public Cell<String> getCell() {
				return new TextCell(new SafeHtmlRenderer<String>() {
					@Override
					public void render(String object, SafeHtmlBuilder builder) {
						builder.appendEscaped("<").appendEscaped(object).appendEscaped(">");
					}

					@Override
					public SafeHtml render(String object) {
						return SafeHtmlUtils.fromString("<" + object + ">");
					}
				});
			}
		};
		//cells.add(tagName);
		HasCell<Node, Node> element = new HasCell<Node, Node>() {
			@Override
			public Cell<Node> getCell() {
				return new ElementCell();
			}

			@Override
			public FieldUpdater<Node, Node> getFieldUpdater() {
				return null;
			}

			@Override
			public Node getValue(Node object) {
				return object;
			}
		};
		cells.add(element);

		nodeCell = new CompositeCell<Node>(cells);
	}
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		Node node = (Node)value;
		ListDataProvider<Node> dataProvider = new ListDataProvider<Node>(getChildren(node));
		return new DefaultNodeInfo<Node>(dataProvider, nodeCell);
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
				list.add(node.getAttributes().item(i));
			}
		}
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			list.add(node.getChildNodes().item(i));
		}

		return list;
	}
	private static class ViewState {
		enum Section { TagName, AttributeName, AttributeValue, Content, None }
		public Section section;
		//public String sectionName;

		public String value;
	}
	public static class ElementCell extends AbstractEditableCell<Node, ViewState> {
		interface Template extends SafeHtmlTemplates {
			@Template("<span>&lt;<input type=\"text\" value=\"{0}\" />&gt;</span>")
			SafeHtml element(String nodeName);
			@Template("<span><input type=\"text\" value=\"{0}\" /> = <input type=\"text\" value=\"{1}\" /></span>")
			SafeHtml attribute(String name, String value);
			@Template("<span>#text <textarea>{0}</textarea></span>")
			SafeHtml text(String contents);
			@Template("<span>#cdata <textarea>{0}</textarea></span>")
			SafeHtml cdata(String contents);
		}
		private static Template template;
		private Object lastKey;


		public ElementCell() {
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
			} else if (value instanceof CharacterData) {
				sb.append(template.cdata(value.getNodeValue()));
			} else if (value instanceof Text) {
				sb.append(template.text(value.getNodeValue()));
			} else if (value instanceof Attr) {
				sb.append(template.attribute(((Attr)value).getName(), value.getNodeValue()));
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
				assert value instanceof CharacterData || value instanceof Text;
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



		}

		@Override
		protected void onEnterKeyDown(Cell.Context context,
				com.google.gwt.dom.client.Element parent, Node value,
				NativeEvent event, ValueUpdater<Node> valueUpdater) {
			//if the event is directed at an input, finish editing, otherwise focus on first elt
			com.google.gwt.dom.client.Element target = event.getEventTarget().cast();
			//TODO
		}
	}
}
