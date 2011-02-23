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

import com.google.gwt.i18n.client.Messages;

/**
 * @author colin
 *
 */
public interface ValidationMessages extends Messages {
	@DefaultMessage("Node {0} may not have child {1}")
	String tagNotAllowedThere(@Optional String parent, String tag);

	@DefaultMessage("Node {0} may not have attribute {1}")
	String attributeNotAllowedThere(@Optional String parentName, String attrName);

	@DefaultMessage("Attribute {0} may not appear more than once in a node")
	String multipleAttributesPresent(String attrName);

	//...
}
