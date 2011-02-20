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

import com.colinalworth.xmlview.client.tree.XmlTreeViewModel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * @author colin
 *
 */
public class XmlViewerEntryPoint implements EntryPoint {
	private CellTree t;

	@Override
	public void onModuleLoad() {
		XmlTreeViewModel viewModel = new XmlTreeViewModel();


		Document rootValue = XMLParser.parse("<zones><zone name='Bedrooms' id='6'><sensor id='1' name='Hallway'>string content!</sensor><sensor id='2' name='Master'></sensor></zone><zone name='Living Area' id='8'><sensor id='3' /></zone><zone name='Garage' id='9'><sensor id='4' /></zone><zone name='Basement' id='10'><sensor id='5' /></zone></zones>");

		t = new CellTree(viewModel, rootValue);
		RootPanel.get().add(t);


	}

	public void validate() {
		Document d = (Document)t.getRootTreeNode().getValue();


		//pass to server
		d.toString();


	}
	public void save() {
		Document d = (Document)t.getRootTreeNode().getValue();


		//pass to server
		d.toString();


	}

}
