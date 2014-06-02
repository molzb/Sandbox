<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>File Upload Test</title>
		<script type="text/javascript" src="tf/common/framework/jquery/jquery-1.7.1.min.js"></script>
		<script type="text/javascript" src="//code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
		<link rel="stylesheet" href="//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css">
		<style type="text/css">
			body {
				font-family: Arial, sans-serif;
				font-size: 13px;
				line-height: 16px;
			}
		</style>
    </head>
    <body data-context-path="/Sandbox/">
		<c:choose>
			<c:when test="${empty param.menu_selection or param.menu_selection eq 'sandbox_fileupload'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample1'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_1.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample2'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_2.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample3'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_3.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample4'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_4.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample5'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_5.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample6'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_6.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsample7'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_7.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'fileuploadsamplejq'}">
				<jsp:include page="content/samples/fileupload/FileUpload2014Test_jq.jsp" flush="true"/>
			</c:when>
			<c:when test="${param.menu_selection eq 'batchUser'}">
				<jsp:include page="batchUser.jsp" flush="true"/>
			</c:when>
		</c:choose>
    </body>
</html>
