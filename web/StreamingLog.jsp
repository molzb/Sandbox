<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Log Viewer</title>
		<link href="resources/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
		<link href="resources/css/streamingLog.css" rel="stylesheet" type="text/css"/>
		<script type="text/javascript" src="js/jquery-1.11.1.js"></script>
		<script type="text/javascript" src="resources/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="resources/js/streamingLog.js"></script> 
	</head>
	<body>
		<h2>Log Viewer</h2>

		<ul class="nav nav-tabs" role="tablist">
			<li class="active" id="tabCatalina">
				<a href="#catalina" role="tab" data-toggle="tab">catalina.out</a>
			</li>
			<li id="tabTomcatBase">
				<a href="#tomcat_base" role="tab" data-toggle="tab">tomcat_base.log</a>
			</li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="catalina">
				<jsp:include page="StreamingLogContent.jsp">
					<jsp:param name="file" value="catalina.out"/>
				</jsp:include>
			</div>
			<div class="tab-pane" id="tomcat_base">
				<jsp:include page="StreamingLogContent.jsp">
					<jsp:param name="file" value="tomcat_base.log"/>
				</jsp:include>
			</div>
		</div>
	</body>
</html>