

<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>


<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.mediator.urlrewrite.URLRewriteMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory"%>
<%@ page import="org.apache.synapse.util.AXIOMUtils"%>
<%@ page import="javax.xml.stream.XMLStreamException"%>
<%@page import="org.apache.synapse.util.xpath.SynapseXPath"%>
<%
		Mediator mediator = SequenceEditorHelper.getEditingMediator(request,
		                                                            session);

		if (!(mediator instanceof URLRewriteMediator)) {

				throw new RuntimeException("不能编辑中介");
		}
		URLRewriteMediator urlRewriteMediator = (URLRewriteMediator) mediator;

		if (request.getParameter("in.property") != null &&
		    !request.getParameter("in.property").trim().equals("")) {
				urlRewriteMediator.setInProperty(request.getParameter("in.property"));
		}
		if (request.getParameter("out.property") != null &&
		    !request.getParameter("out.property").trim().equals("")) {
				urlRewriteMediator.setOutProperty(request.getParameter("out.property"));
		}
%>

