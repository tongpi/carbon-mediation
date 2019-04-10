<%@ page import="org.wso2.carbon.task.ui.internal.ResponseInformation" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String className = request.getParameter("taskClass");
    String states = "";
    String group = request.getParameter("taskGroup");

    if (className != null && !"".equals(className) && group != null && !"".equals(group)) {
        TaskManagementClient client;
        try {
            client = TaskManagementClient.getInstance(config, session);
            ResponseInformation responseInformation = client.loadTaskProperties(className, group);
            if (responseInformation.isFault()) {
                states = responseInformation.getMessage();
            }
        }
        catch (Throwable e) {
            states = e.getMessage();
        }
    }
%>
<%=states%>