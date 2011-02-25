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
package com.colinalworth.xmlview.client.schema;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import com.google.gwt.xml.client.Node;

/**
 * 
 * @todo provide type restriction data for elements and attributes
 * @todo 
 * @author colin
 *
 */
public interface Schema {
	public interface NamespacedNode {
		public String getNamespaceURI();
		public String getNodeName();
	}
	NamespacedNode createNode(Node node);
	NamespacedNode createNode(String uri, String name);

	Set<NamespacedNode> getRootNodes();

	Set<NamespacedNode> getAttributes(NamespacedNode element);
	Set<NamespacedNode> getAttributes(String namespace, String elementName);

	Set<NamespacedNode> getChildElements(NamespacedNode element);
	Set<NamespacedNode> getChildElements(String namespace, String elementName);




	/**
	 * 
	 * @author colin
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SchemaURL {
		String[] value();
	}

	/**
	 * 
	 * @author colin
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SchemaPath {
		String[] value();
	}
}
