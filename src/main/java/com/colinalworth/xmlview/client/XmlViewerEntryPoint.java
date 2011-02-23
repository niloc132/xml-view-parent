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
import com.colinalworth.xmlview.client.validator.XmlValidator;
import com.colinalworth.xmlview.client.validator.XmlValidator.SchemaURL;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * @author colin
 *
 */
public class XmlViewerEntryPoint implements EntryPoint {
	@SchemaURL({"http://fisheye.jboss.org/browse/~raw,r=35924/JBossRules/trunk/drools-container/drools-spring/src/main/resources/org/drools/container/spring/drools-spring-1.2.0.xsd", "http://www.springframework.org/schema/beans/spring-beans-2.0.xsd"})
	interface DroolsSpringSchema extends XmlValidator {}

	@SchemaURL("http://maven.apache.org/xsd/maven-4.0.0.xsd")
	interface MavenPom extends XmlValidator {}
	interface Binder extends UiBinder<HTMLPanel, XmlViewerEntryPoint> {}

	private Binder binder = GWT.create(Binder.class);
	@UiField(provided = true) CellTree tree;

	@Override
	public void onModuleLoad() {
		Document rootValue = XMLParser.parse("<beans xmlns='http://www.springframework.org/schema/beans' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'	xmlns:drools='http://drools.org/schema/drools-spring'	xsi:schemaLocation='http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd                        http://drools.org/schema/drools-spring org/drools/container/spring/drools-spring-1.2.0.xsd'> 	<bean id='ds' class='org.springframework.jdbc.datasource.DriverManagerDataSource'> 		<!-- org.h2.jdbcx.JdbcDataSource --> 		<property name='driverClassName' value='org.h2.Driver' /> 			<property name='url' value='jdbc:h2:tcp://localhost/DroolsFlow' /> 		<property name='username' value='sa' /> 		<property name='password' value='' /> 	</bean> 	<drools:grid-node id='node1' /> <drools:kbase id='kbProcessWorkItems' node='node1'><drools:resources> <drools:resource type='PKG' source='file:///${temp.dir}/processWorkItems.pkg' /> <drools:resource type='PKG' source='file:///${temp.dir}/processSubProcess.pkg' /> <drools:resource type='PKG' source='file:///${temp.dir}/processTimer.pkg' /> <drools:resource type='PKG' source='file:///${temp.dir}/processTimer2.pkg' /> </drools:resources> </drools:kbase></beans>");

		// passing in the full document, as the children of the first element are parsed, not the root itself
		//DroolsSpringSchema schema = GWT.create(DroolsSpringSchema.class);

		MavenPom pom = GWT.create(MavenPom.class);

		XmlTreeViewModel viewModel = new XmlTreeViewModel();
		tree = new CellTree(viewModel, rootValue);
		RootPanel.get().add(binder.createAndBindUi(this));


	}

	public void load() {

	}

	public void switchToText() {

	}
	public void switchToTree() {

	}

	@UiHandler("validate")
	public void validate(ClickEvent evt) {
		Document d = (Document)tree.getRootTreeNode().getValue();


		//pass to server
		Window.alert(d.toString());


	}
	@UiHandler("save")
	public void save(ClickEvent evt) {
		Document d = (Document)tree.getRootTreeNode().getValue();


		//pass to server
		d.toString();


	}

}
