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

import com.colinalworth.xmlview.client.schema.Schema;
import com.colinalworth.xmlview.client.schema.Schema.NamespacedNode;
import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;

/**
 * @author colin
 *
 */
public class SchemaBasedXmlValidator implements XmlValidator {
	private final Schema schema;
	/**
	 * 
	 */
	public SchemaBasedXmlValidator(Schema schema) {
		this.schema = schema;
	}
	@Override
	public boolean isElementNameValid(Element child) {
		NamespacedNode parent = schema.createNode(child.getParentNode());
		return schema.getChildElements(parent).contains(schema.createNode(child));
		//TODO Schema needs to provide Element order data
	}

	@Override
	public boolean isAttributeNameValid(Attr attr, Element parent) {
		NamespacedNode p = schema.createNode(parent);
		return schema.getAttributes(p).contains(schema.createNode(attr));
	}

	@Override
	public boolean isAttributeValueValid(Attr attr, Element parent) {
		return true;//TODO Schema needs to provide data for this
	}

	@Override
	public boolean isContentsValid(CharacterData data) {
		return true;//TODO Schema needs to provide data for this
	}

}
