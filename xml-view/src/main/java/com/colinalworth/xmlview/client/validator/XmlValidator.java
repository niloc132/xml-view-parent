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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;

/**
 * @author colin
 *
 */
public interface XmlValidator {
	/**
	 * Checks that this element may be in its current position in its parent
	 * 
	 * @todo deal with namespace
	 * @param child
	 * @return
	 */
	boolean isElementNameValid(Element child);

	/**
	 * 
	 * @param attr
	 * @return
	 */
	boolean isAttributeNameValid(Attr attr);

	/**
	 * Checks that the value of the given attribute is consistent with the Element it is in
	 * format allowed
	 * @param attr
	 * @return
	 */
	boolean isAttributeValueValid(Attr attr);

	/**
	 * Checks to see if the data (CData or Text) are valid, both if allowed
	 * to be in the parent Node, and if the format/type is correct
	 * @param data
	 * @return
	 */
	boolean isContentsValid(CharacterData data);

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SchemaURL {
		String[] value();
	}
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SchemaPath {
		String[] value();
	}
}