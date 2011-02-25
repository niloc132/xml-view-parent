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
package com.colinalworth.xmlview.client.validator;

import com.colinalworth.xmlview.shared.SchemaModel;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;

/**
 * @author colin
 *
 */
public class SimpleRpcXmlValidator implements XmlValidator {
	private SchemaModel schema;
	/**
	 * 
	 */
	public SimpleRpcXmlValidator(SchemaModel schema) {
		this.schema = schema;
	}

	@Override
	public boolean isElementNameValid(Element child) {
		return schema.getAllowedChildrenElements().get(child.getParentNode().getNodeName()).contains(child.getNodeName());
	}


	@Override
	public boolean isAttributeNameValid(Attr attr, Element parent) {
		//TODO make sure there are no attr duplicates (this is never legal, correct?)
		return schema.getAllowedAttributes().get(parent.getNodeName()).contains(attr.getName());
	}


	@Override
	public boolean isAttributeValueValid(Attr attr, Element parent) {
		//TODO
		return true;
	}


	@Override
	public boolean isContentsValid(CharacterData data) {
		//TODO data type validation
		return schema.getCanHaveCData().containsKey(data.getParentNode().getNodeName());
	}

}
