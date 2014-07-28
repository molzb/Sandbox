<%@page import="org.springframework.http.HttpEntity"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="java.io.File"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%--
    Document   : test.jsp
--%>
<%-- einzeiliger JSP-Kommentar --%>

<%
	int dummy = 0;
	// das ist ein Kommentar
	// Der FileUsageScanner sollte die Kommentare rausschneiden
	/**
	 * Übrig bleibt dann nur noch dummy...
	 */
	dummy = 1;
	/** Einzeiliger Kommentar*/
	dummy = 2;
	// nochmal
	dummy = 3;
%>

<!DOCTYPE html>

<jsp:useBean id="userAdminCmd" class="bm.scanner.DirWalker" scope="request"/>
<%@  include file="includedJsp.jsp"%>
<jsp:include page="includedJspInclude.jsp"/>
<%	RequestDispatcher rd = request.getRequestDispatcher("includedReqDispatcher");	%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link href="../resources/css/FileUpload2014.css" type="text/css" rel="stylesheet">
		<link href="../resources/css/bootstrap.min.css" type="text/css" rel="stylesheet">
		<style>
			@import url("../resources/jstree/themes/default/style.min.css");
		</style>
		<script src="../resources/js/batchUser.js"></script>
		<script src="../js/amcharts.js"></script>
        <title>JSP Page</title>
    </head>
    <body>
        <%
			// Test for imports
			// The scanner should recognize, that SLF4j, Commons IO and Spring Web MVC is used
			Logger logger = LoggerFactory.getLogger("test");	//slf4j
			File[] f1 = FileUtils.EMPTY_FILE_ARRAY;	// Commons IO
			HttpEntity e;	// Spring Web MVC, so don't read commons.lang.whatever
			String s = "Don't read //comment here"; 
			String t = "Don't read //comment here"; // but here
			String u = "This is not a /*comment*/";
			String v = "This is not a /*comment*/"; /* but this is */
			/** Single line comment in code block*/
			/*
			 * Multiline comment in code block
			 */
			if(!com.db.tomcat.tfsecurity.TFSecurity.getTFUser().isPermitted("user_administration")) return;
			new java.util.concurrent.BrokenBarrierException();
			java.awt.Component c1d2 = new java.awt.List();
			java.util.Set<Logger> loggers = new java.util.HashSet<>();
			com.db.tomcat.tfsecurity.TFUser user;
			com.db.tradefinder.TradeFinderServlet /* com.db.dontReadThis */ tfs;	// and not that
			// static calls
			org.springframework.util.StringUtils.capitalize("X");
			String tr = com.db.tradefinder.service.factory.ConnectionFactory.TRADEFINDER;
		%>
		<h1>Hello World!</h1>
    </body>
</html>
