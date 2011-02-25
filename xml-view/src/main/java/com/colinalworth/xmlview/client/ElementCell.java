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

import com.colinalworth.xmlview.client.XmlTreeViewModel.XmlEditPopupPanel;
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
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Comment;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Text;

/**
 * 
 * @author colin
 *
 */
public class ElementCell extends AbstractEditableCell<Node, ElementCell.ViewState> {
	interface Templates extends SafeHtmlTemplates {
		@Template("<span class='data'>&lt;<input type=\"text\" value=\"{0}\" />&gt;</span><span class='actions'>[V]</span>")
		SafeHtml element(String nodeName);
		@Template("<span><input class=\"attrName\" type=\"text\" value=\"{0}\" /> = <input class=\"attrValue\" type=\"text\" value=\"{1}\" /></span>")
		SafeHtml attribute(String name, String value);
		@Template("<span>#text <textarea>{0}</textarea></span>")
		SafeHtml text(String contents);
		@Template("<span>#cdata <textarea>{0}</textarea></span>")
		SafeHtml cdata(String contents);
		@Template("&lt;--{0}--&gt;")
		SafeHtml comment(String contents);
	}

	static class ViewState {
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

		public String value;
	}

	private static Templates template;
	private final XmlEditPopupPanel menu;

	private final XmlValidator validator;
	private Object lastKey;


	public ElementCell(XmlValidator validator, XmlEditPopupPanel menu) {
		super("blur", "focus", "keydown", "keyup", "click");
		if (template == null) {
			template = GWT.create(Templates.class);
		}
		this.validator = validator;
		this.menu = menu;
	}

	@Override
	public void render(Cell.Context context, Node value, SafeHtmlBuilder sb) {
		Object key = context.getKey();
		ElementCell.ViewState viewData = getViewData(key);
		if (viewData != null && viewData.section != ElementCell.ViewState.Section.None &&
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
	private boolean viewDataMatchesValue(ElementCell.ViewState viewData, Node value) {
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

		String eventType = event.getType();
		if ("click".equals(eventType) && target.getClassName().equals("actions")) {
			menu.setPopupPosition(event.getClientX(), event.getClientY());
			menu.show(value);
			return;
		}

		// Ignore events that don't target the input.
		if (!"INPUT".equals(target.getTagName()) && !target.getTagName().equals("TEXTAREA")) {
			return;
		}

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
			valueUpdater.update(value);
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
		ElementCell.ViewState state = updateViewState(lastKey, value, target);
		String newValue = target.<InputElement>cast().getValue();
		boolean valid = true;
		Element parent = (Element)value.getParentNode();
		switch (state.section) {
		case AttributeName:
			Attr attr = (Attr) value;
			//TODO this might lose namespace data
			parent.removeAttribute(attr.getName());
			parent.setAttribute(newValue, attr.getValue());


			valid = validator.isAttributeNameValid(parent.getAttributeNode(attr.getName()), parent);
			break;
		case AttributeValue:
			value.setNodeValue(newValue);
			valid = validator.isAttributeValueValid((Attr) value, parent);
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

	private ElementCell.ViewState updateViewState(Object key, Node value,
			com.google.gwt.dom.client.Element target) {
		ElementCell.ViewState viewState = getViewData(key);
		if (viewState == null) {
			viewState = new ElementCell.ViewState();
			setViewData(key, viewState);
		}
		viewState.section = ElementCell.ViewState.Section.getViewState(target, value);
		viewState.value = target.<InputElement>cast().getValue();
		return viewState;
	}
}