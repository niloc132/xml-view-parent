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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
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
		if (node instanceof Element || node instanceof Document) {
			//is not a leaf if it has children, or if it has attributes
			return !node.hasChildNodes()/* && !node.hasAttributes()*/;
		}
		//if not a Node, it is a leaf
		return true;
	}

	private List<Node> getChildren(Node node) {
		List<Node> list = new ArrayList<Node>();

		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			list.add(node.getChildNodes().item(i));
		}
		return list;
	}
	private class ElementCell extends AbstractCell<Node> {
		@Override
		public void render(Cell.Context context, Node value, SafeHtmlBuilder sb) {
			sb.appendHtmlConstant("<table><thead><tr><td>").appendEscaped("<" + value.getNodeName() + ">").appendHtmlConstant("</td></tr></thead>");
			sb.appendHtmlConstant("<tbody><tr><td></td>");
			if (value instanceof Element) {
				for (int i = 0; i < value.getAttributes().getLength(); i++) {
					sb.appendHtmlConstant("<td>");
					renderAttr((Attr)value.getAttributes().item(i), sb);
					sb.appendHtmlConstant("</td>");
				}
			} else if (value instanceof CharacterData || value instanceof Text) {
				//this is probably wrong for cdata
				sb.appendHtmlConstant("<td>").appendEscaped(value.getNodeValue()).appendHtmlConstant("</td>");
			}
			sb.appendHtmlConstant("</tr></tbody></table>");
		}

		private void renderAttr(Attr item, SafeHtmlBuilder sb) {
			sb.appendEscaped(item.getName()).appendEscaped("=").appendEscaped(item.getValue());
		}
	}
}
