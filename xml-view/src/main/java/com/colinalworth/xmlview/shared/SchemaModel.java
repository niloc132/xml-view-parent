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
package com.colinalworth.xmlview.shared;

import java.util.Map;
import java.util.Set;

/**
 * Basic schema model for transport over RPC
 * 
 * @todo deal with namespaces
 * @author colin
 *
 */
public class SchemaModel {
	private String rootElement;
	private Map<String, Set<String>> allowedChildrenElements;
	private Map<String, Set<String>> allowedAttributes;
	private Map<String, Boolean> canHaveCData;
	public String getRootElement() {
		return rootElement;
	}
	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public Map<String, Set<String>> getAllowedChildrenElements() {
		return allowedChildrenElements;
	}
	public void setAllowedChildrenElements(
			Map<String, Set<String>> allowedChildrenElements) {
		this.allowedChildrenElements = allowedChildrenElements;
	}

	public Map<String, Set<String>> getAllowedAttributes() {
		return allowedAttributes;
	}
	public void setAllowedAttributes(Map<String, Set<String>> allowedAttributes) {
		this.allowedAttributes = allowedAttributes;
	}

	public Map<String, Boolean> getCanHaveCData() {
		return canHaveCData;
	}
	public void setCanHaveCData(Map<String, Boolean> canHaveCData) {
		this.canHaveCData = canHaveCData;
	}

}
