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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.colinalworth.xmlview.client.validator.AcceptAllXmlValidator;
import com.colinalworth.xmlview.client.validator.XmlValidator;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * @author colin
 *
 */
public class XmlTreeViewModel implements TreeViewModel {
	public class XmlEditPopupPanel extends PopupPanel {
		private Node node;
		public XmlEditPopupPanel() {
			super(true, true);
		}
		public void show(Node node) {
			this.node = node;
			show();
		}
	}
	abstract class ElementModificationCommand<N extends Node> implements Command {
		@Override
		public final void execute() {
			N node = (N) contextMenu.node;
			Node parent = node.getParentNode();
			modify(node);
			redrawChildren(node, parent);

			contextMenu.hide();
		}
		public abstract void modify(N node);
	}
	class NewElementCommand extends ElementModificationCommand<Element> {
		@Override
		public void modify(Element node) {
			node.appendChild(node.getOwnerDocument().createElement("new-element"));
		}
	}
	class NewAttrCommand extends ElementModificationCommand<Element> {
		@Override
		public void modify(Element node) {
			node.setAttribute("new-attribute", "");
		}
	}
	class NewTextCommand extends ElementModificationCommand<Element> {
		@Override
		public void modify(Element node) {
			node.appendChild(node.getOwnerDocument().createTextNode("--sample content--"));
		}
	}

	class DeleteElementCommand extends ElementModificationCommand<Element> {
		@Override
		public void modify(Element node) {
			Node parent = node.getParentNode();
			parent.removeChild(node);
		}
	}
	private final XmlTreeMenuOptions i18n = GWT.create(XmlTreeMenuOptions.class);
	private final ElementCell nodeCell;
	private final XmlEditPopupPanel contextMenu;
	private final Map<Node, ValueUpdater<Node>> refreshAccess;
	/**
	 * 
	 */
	public XmlTreeViewModel() {
		this(new AcceptAllXmlValidator());
	}
	/**
	 * 
	 * @param validator
	 */
	public XmlTreeViewModel(XmlValidator validator) {
		final MenuBar subMenu = new MenuBar(true);

		subMenu.addItem(i18n.delete(), new DeleteElementCommand());
		subMenu.addSeparator();
		subMenu.addItem(i18n.addElement(), new NewElementCommand());
		subMenu.addItem(i18n.addAttr(), new NewAttrCommand());
		subMenu.addItem(i18n.addText(), new NewTextCommand());
		contextMenu = new XmlEditPopupPanel();

		contextMenu.setWidget(subMenu);
		contextMenu.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				subMenu.selectItem(null);
			}
		});

		nodeCell = new ElementCell(validator, contextMenu);

		refreshAccess = new HashMap<Node, ValueUpdater<Node>>();
	}
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		final Node node = (Node)value;
		final ListDataProvider<Node> dataProvider = new ListDataProvider<Node>(getChildren(node));
		ValueUpdater<Node> parentUpdater = new ValueUpdater<Node>() {
			@Override
			public void update(Node ignoreThis) {
				dataProvider.setList(getChildren(node));
			}
		};
		refreshAccess.put(node, parentUpdater);
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

	/**
	 * Forces a redraw of the element, or its first visible ancestor
	 * 
	 * @param node
	 * @param parent
	 */
	public void redrawChildren(Node node, Node parent) {
		ValueUpdater<Node> updater = refreshAccess.get(node);
		while (updater == null) {
			updater = refreshAccess.get(parent);
			parent = parent.getParentNode();
		}
		updater.update(null);
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
