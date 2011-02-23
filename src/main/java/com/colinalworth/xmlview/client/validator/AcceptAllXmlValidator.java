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

import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;

/**
 * Default xml validator, which allows any and all content.
 * 
 * @author colin
 *
 */
public class AcceptAllXmlValidator implements XmlValidator {
	@Override
	public boolean isElementNameValid(Element child) {
		return true;
	}
	@Override
	public boolean isAttributeNameValid(Attr attr) {
		return true;
	}
	@Override
	public boolean isAttributeValueValid(Attr attr) {
		return true;
	}
	@Override
	public boolean isContentsValid(CharacterData data) {
		return true;
	}
}
