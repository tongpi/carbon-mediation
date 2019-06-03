<%--
~  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  Licensed to the Apache Software Foundation (ASF) under one or more
~  contributor license agreements.  See the NOTICE file distributed with
~  this work for additional information regarding copyright ownership.
~
~  The ASF licenses this file to You under the Apache License, Version 2.0
~
~  (the "License"); you may not use this file except in compliance with
~  the License.  You may obtain a copy of the License at
~       http://www.apache.org/licenses/LICENSE-2.0
~  Unless required by applicable law or agreed to in writing, software
~  distributed under the License is distributed on an "AS IS" BASIS,
~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~  See the License for the specific language governing permissions and
~  limitations under the License.
--%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.sink.xsd.EventSink" %>
<%@ page import="org.wso2.carbon.event.sink.config.ui.PublishEventMediatorConfigAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    response.setHeader("Cache-Control", "no-cache");
%>


<script>
    function eventSinkValidate() {
        var name = document.getElementById('propertyName0');
        if (name && name.value == "") {
            CARBON.showErrorDialog(eventSinki18n["specify.EventSinkName"]);
            return false;
        }
        var username = document.getElementById('propertyUsername0');
        if (username && username.value == "") {
            CARBON.showErrorDialog(eventSinki18n["specify.Username"]);
            return false;
        }

        var password = document.getElementById('propertyPassword0');
        if (password && password.value == "") {
            CARBON.showErrorDialog(eventSinki18n["specify.Password"]);
            return false;
        }
        var receiverUrl = document.getElementById('propertyReceiverUrl0');
        if (receiverUrl && receiverUrl.value == "") {
            CARBON.showErrorDialog(eventSinki18n["specify.ReceiverUrl"]);
            return false;
        }

        var receiverUrlCheck
                = (receiverUrl.value.trim()).match(/^tcp?:\/\/\w+(\.\w+)*(:[0-9]{1,5})(,\s*tcp?:\/\/\w+(\.\w+)*(:[0-9]{1,5}))*/g);
        if (!(receiverUrlCheck == (receiverUrl.value.trim()))) {
            CARBON.showErrorDialog("接收器Url格式无效");
            return false;

        }
        return true;
    }
    function configureEventSink() {

        if (eventSinkValidate()) {

            var username = document.getElementById("propertyUsername0").value;
            var password = document.getElementById("propertyPassword0").value;
            var receiverUrl = document.getElementById("propertyReceiverUrl0").value;
            var authenticatorUrl = document.getElementById("propertyAuthenticatorUrl0").value;
            var propertyCount = document.getElementById("propertyCount").value;
            var action = document.getElementById("action").value;

            if (action == "add") {
                var name = document.getElementById("propertyName0").value;
                CARBON.showConfirmationDialog("确信要添加事件接收器 '" + escape(name) + "'吗?", function () {
                    jQuery.ajax({
                        type: "GET",
                        url: "../event-sink-config/update_event_sink_ajaxprocessor.jsp",
                        data: {action: "add", propertyName0: name, propertyCount: propertyCount, propertyUsername0: username, propertyPassword0: password, propertyReceiverUrl0: receiverUrl, propertyAuthenticatorUrl0: authenticatorUrl},
                        success: function (data) {
                            setTimeout(function () {
                                window.location.href = "event_sinks_configuration.jsp?ordinal=1";
                            }, 5000);
                            CARBON.showInfoDialog("您的事件接收器已部署。请刷新此页以查看新事件接收器的状态.");
                        }
                    });
                });
            } else if (action == "edit") {
                var name = document.getElementById("propertyName0").innerHTML.trim();
                CARBON.showConfirmationDialog("确信要更新事件接收器 '" + escape(name) + "'吗?", function () {
                    jQuery.ajax({
                        type: "GET",
                        url: "../event-sink-config/update_event_sink_ajaxprocessor.jsp",
                        data: {action: "edit", propertyName0: name, propertyCount: propertyCount, propertyUsername0: username, propertyPassword0: password, propertyReceiverUrl0: receiverUrl, propertyAuthenticatorUrl0: authenticatorUrl},
                        success: function (data) {
                            setTimeout(function () {
                                window.location.href = "event_sinks_configuration.jsp?ordinal=1";
                            }, 1000);
                            CARBON.showInfoDialog("您的事件接收器已更新。请刷新此页以查看新事件接收器的状态.");
                        }
                    });
                });
            }

        }
    }

    function testURL(url, protocol) {
        var receiverUrlCheck = (url.trim()).match(/^tcp?:\/\/\w+(\.\w+)*(:[0-9]{1,5})(,\s*tcp?:\/\/\w+(\.\w+)*(:[0-9]{1,5}))*/g);
        var authenticationUrlCheck = (url.trim()).match(/^ssl?:\/\/\w+(\.\w+)*(:[0-9]{1,5})/g);
        if (url == '') {
            CARBON.showErrorDialog("服务器 url不能为空");
        } else if (receiverUrlCheck == null && protocol == "tcp") {
            CARBON.showErrorDialog("不完整的 url 或无效的协议, 请指定正确的 url");
            return false;
        } else if (authenticationUrlCheck == null && protocol == "ssl") {
            CARBON.showErrorDialog("不完整的 url 或无效的协议, 请指定正确的 url");
            return false;
        } else {
            var receivedUrl = /^(.*:)\/\/([A-Za-z0-9\-\.]+)(:([0-9]+))?(.*)$/i;
            var commProtocol = receivedUrl.exec(url)[1];
            var serverHost = receivedUrl.exec(url)[2];
            var serverPort = receivedUrl.exec(url)[3];
            var serverPort = receivedUrl.exec(url)[4];
            if (protocol == "tcp") {
                if (!(receiverUrlCheck == (url.trim())) && !(commProtocol == "tcp:")) {
                    CARBON.showErrorDialog("接收器url格式无效");
                    return false;
                }
            } else {
                if (!(authenticationUrlCheck == (url.trim())) && !(commProtocol == "ssl:")) {
                    CARBON.showErrorDialog("认证url格式无效");
                    return false;
                }
            }
            jQuery.ajax({
                type: "GET",
                url: "../event-sink-config/update_event_sink_ajaxprocessor.jsp",
                data: {action: "testServer", ip: serverHost, port: serverPort},
                success: function (data) {
                    if (data != null && data != "") {
                        var result = data.replace(/\n+/g, '');
                        if (result == "true") {
                            CARBON.showInfoDialog("成功连接到分析服务器.");
                        } else if (result == "false") {
                            CARBON.showErrorDialog("分析服务器连接失败!")
                        }
                    }
                }
            });
        }
    }
</script>

<%
    response.setHeader("Cache-Control", "no-cache");
    String action = request.getParameter("action");


    EventSink eventSink = null;
    if (action.equals("edit")) {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        PublishEventMediatorConfigAdminClient publishEventMediatorConfigAdminClient =
                new PublishEventMediatorConfigAdminClient(cookie, backendServerURL, configContext, request.getLocale());
         eventSink =
                 publishEventMediatorConfigAdminClient.getEventSinkByName(request.getParameter("name"));
    }else if(action.equals("add")){
        eventSink = new EventSink();
        eventSink.setName("");
        eventSink.setUsername("");
        eventSink.setPassword("");
        eventSink.setReceiverUrlSet("");
        eventSink.setAuthenticationUrlSet("");
    }

%>

<fmt:bundle basename="org.wso2.carbon.event.sink.config.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.event.sink.config.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="eventSinki18n"/>
    <carbon:breadcrumb
            label="publishEvent.configuration.breadcrumb"
            resourceBundle="org.wso2.carbon.event.sink.config.ui.i18n.JSResources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">

        <div id="workArea">

            <table class="normal" width="100%">
                <tr>
                    <td>
                        <h2>
                            <%
                                if (action.equals("add")) {
                                    out.print("Add Event Sink");
                                } else {
                                    out.print("Edit Event Sink");
                                }
                            %>
                        </h2>
                    </td>
                </tr>

                <tr>
                    <td>


                        <div style="margin-top:0px;">

                            <table id="propertytable" class="styledLeft">
                                <tbody id="propertytbody">
                                <%
                                    int i = 0;

                                %>
                                <tr>

                                        <%
                                            if (!action.equals("edit")) {
                                        %>
                                    <td width="15%"><fmt:message key="publishEvent.configuration.attribute.name"/><span class="required">*</span></td>
                                    <td>

                                        <input style="width: 300px;" type="text" name="propertyName0" id="propertyName0"
                                               class="esb-edit small_textbox"
                                               value="<%=eventSink.getName()%>"/>
                                        <%
                                        } else {
                                        %>
                                        <td width="15%"><fmt:message key="publishEvent.configuration.attribute.name"/></td>
                                        <td>
                                        <div id="propertyName0"><%=eventSink.getName()%>
                                        </div>
                                        <%
                                            }
                                        %>

                                    </td>
                                </tr>
                                <tr>
                                    <td width="15%"><fmt:message
                                            key="publishEvent.configuration.attribute.username"/><span class="required">*</span></td>
                                    <td><input style="width: 300px;" type="text" name="propertyUsername0"
                                               id="propertyUsername0"
                                               class="esb-edit small_textbox"
                                               value="<%=eventSink.getUsername()%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="15%"><fmt:message
                                            key="publishEvent.configuration.attribute.password"/><span class="required">*</span></td>
                                    <td><input style="width: 300px;" type="password" name="propertyPassword0"
                                               id="propertyPassword0"
                                               class="esb-edit small_textbox"
                                               value="<%=eventSink.getPassword()%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="15%"><fmt:message
                                            key="publishEvent.configuration.attribute.receiverUrl"/><span class="required">*</span></td>
                                    <td><input style="width: 600px;" type="text" name="propertyReceiverUrl0"
                                               id="propertyReceiverUrl0"

                                               value="<%=eventSink.getReceiverUrlSet()%>"/>
                                        <input id="testReceiverAddress" name="testReceiverAddress" type="button" class="button" onclick="testURL(document.getElementById('propertyReceiverUrl0').value, 'tcp')" value="测试连接">
                                    </td>
                                </tr>
                                <tr>
                                    <td width="15%"><fmt:message
                                            key="publishEvent.configuration.attribute.authenticatorUrl"/></td>
                                    <td><input style="width: 600px;" type="text" name="propertyAuthenticatorUrl0"
                                               id="propertyAuthenticatorUrl0"

                                               value="<%=eventSink.getAuthenticationUrlSet()%>"/>
                                        <input id="testAuthenticatorAddress" name="testAuthenticatorAddress" type="button" class="button" onclick="testURL(document.getElementById('propertyAuthenticatorUrl0').value, 'ssl')" value="测试连接">
                                    </td>
                                </tr>
                                <%

                                %>
                                <input type="hidden" name="propertyCount" id="propertyCount" value="0"/>
                                <input type="hidden" name="action" id="action" value="<%=action%>"/>

                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
        <tr>
            <td>
                <div style="margin-top:30px;margin-left: 25px;margin-right:30px;display: inline;">
                                    <span><a onClick='javaScript:configureEventSink();' style='background-image:
                                        url(images/save-button.gif);' class='icon-link addIcon'>保存</a></span>

                </div>
                <div style="margin-top:30px;display: inline;">
                                    <span><a href="event_sinks_configuration.jsp?ordinal=1" style='background-image:
                                        url(../admin/images/cancel.gif);' class='icon-link addIcon'>取消</a></span>

                </div>
            </td>

        </tr>
    </div>
</fmt:bundle>