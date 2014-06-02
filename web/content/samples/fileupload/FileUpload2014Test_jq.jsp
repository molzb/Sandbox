<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<jsp:include page="FileUpload2014TestInclude.jspf"/>
<link rel="stylesheet" type="text/css" href="resources/css/FileUpload2014.css">
<script src="resources/js/fileUpload/FileUpload2014.js" type="text/javascript"></script>
<script type="text/javascript">jQuery(document).ready(function() { jQuery("#uploadTest").hide(); });</script>

If you want full control over the file upload, you can use the 
<a href="http://blueimp.github.io/jQuery-File-Upload/">JQuery File Upload</a> directly, without resorting to the tag.<br/><br/>

<div class="tagSource">
	JQuery Fileupload:
	<pre>
	jQuery('#fileupload').fileupload({
		url: 'fileupload/uploadFileToServer.disp',
		sequentialUploads: true
	});
	</pre>
</div>
<div class="htmlSource">
	<a href="#"	onclick="upload.toggleHtmlSource()">+ Show generated HTML</a>
	<pre>&nbsp;</pre>
</div>
