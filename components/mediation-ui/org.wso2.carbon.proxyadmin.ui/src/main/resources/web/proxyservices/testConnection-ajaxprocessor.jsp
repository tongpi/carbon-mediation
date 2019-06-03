<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URI" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String url = request.getParameter("url");
    String returnValue;

    if (url != null && !url.equals("")) {
        try {
            URI connUri = new URI(url);
            URL conn = connUri.toURL();
            conn.getContent();
            returnValue = "success";

        } catch (Exception e) {
            returnValue = "指定的WSDL URL无效. URI " + Encode.forHtmlContent(url) + " 格式不正确 " +
                    "或不存在.";
        }
    } else {
        returnValue = "请指定有效的 URL.";
    }
%>
<%=returnValue%>