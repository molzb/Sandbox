<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<jsp:include page="FileUpload2014TestInclude.jspf"/>
		<%-- Verbotener Fileupload simulieren --%>
		<%-- Multiple: Mehrere Dateien (gültig + ungültig) hochladen - stimmen Buttons? --%>
		<%-- Preview txt testen --%>