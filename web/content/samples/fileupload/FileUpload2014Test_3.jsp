<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<script type="text/javascript">
	jQuery(document).ready(function() { 
		upload.highlight("javascriptCallback"); upload.highlight("javaCallback"); 
		jQuery("#uploadTest").hide(); 
	});
</script>

<jsp:include page="FileUpload2014TestInclude.jspf"/>
	
<tf:upload suffices="csv|txt" maxsize="400" uploadCmd="histsim" 
				   filenamePrefix="${time}"
				   javascriptCallback="helloGlobal"
				   javaCallback="com.db.tradefinder.taglib.FileUpload2014Tag.hello"/>
Just like Example 2, but with the attributes 'javaCallback' and 'javascriptCallback'.<br/>
Both values must be methods without parameters. Don't put parentheses behind the method name,
the method name should be "yourMethod", not "yourMethod<b>()</b>".<br/><br/>
<b>Javascript</b>: The JS object <b>upload.uploadedFiles</b> delivers information to the file you have just uploaded. 
Type <code class="js">upload.uploadedFiles</code> in your JS console AFTER the upload.<br/>
	<b>Java</b>: The uploaded files will be stored in the session attribute 'uploadedFiles' as 
	<code>List&lt;File&gt;</code>, so you can access it with 
	<ul>
		<li><code>&#36;{sessionScope.uploadedFiles}</code> in JSP or</li>
		<li><code>request.getAttribute("uploadedFiles")</code> in Java.</li>
	</ul>
	<c:if test="${!empty sessionScope.uploadedFiles}">
		Content of the session attribute: <code>${sessionScope.uploadedFiles}</code><br/>
	</c:if>
<br/><br/>

<div class="tagSource">
	Upload Tag:
	<pre>
	&lt;%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %&gt;
	&lt;tf:upload suffices="csv|txt" maxsize="400" uploadCmd="histsim" 
				   filenamePrefix="${time}"
				   javascriptCallback="helloGlobal"
				   javaCallback="com.db.tradefinder.taglib.FileUpload2014Tag.hello"/&gt;
	</pre>
</div>
<div class="htmlSource">
	<a href="#"	onclick="upload.toggleHtmlSource()">+ Show generated HTML</a>
	<pre>&nbsp;</pre>
</div>
