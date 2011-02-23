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
package com.colinalworth.xmlview.client.validator.impl;

import com.colinalworth.xmlview.client.validator.XmlValidator;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;

/**
 * @author colin
 *
 */
public abstract class AbstractXmlValidatorImpl implements XmlValidator {
	@Override
	public final boolean isElementNameValid(Element child) {
		if (!(child.getParentNode() instanceof Element)) {
			//TODO check if this can be root node
			return true;
		}
		return canElementHaveChildWithName((Element)child.getParentNode(), child.getNodeName()) && 
		isElementAllowedInCurrentPosition((Element)child.getParentNode(), child);
	}

	@Override
	public final boolean isAttributeNameValid(Attr attr) {
		return canElementHaveAttributeWithName((Element)attr.getParentNode(), attr.getName());
	}

	@Override
	public final boolean isAttributeValueValid(Attr attr) {
		return isValueValidInAttributeInElement(attr.getValue(), attr.getName(), (Element)attr.getParentNode());
	}

	@Override
	public final boolean isContentsValid(CharacterData data) {
		return canElementHaveCData((Element)data.getParentNode()) && 
		doesDataMatchRestriction((Element)data.getParentNode(), data.getData());
	}




	protected abstract boolean canElementHaveChildWithName(Element parentNode, String eltName);
	protected abstract boolean isElementAllowedInCurrentPosition(Element parentNode, Element child);

	protected abstract boolean canElementHaveAttributeWithName(Element parentNode, String name);

	protected abstract boolean isValueValidInAttributeInElement(String value, String name, Element parentNode);


	protected abstract boolean doesDataMatchRestriction(Element parentNode, String data);
	protected abstract boolean canElementHaveCData(Element elt);
}
