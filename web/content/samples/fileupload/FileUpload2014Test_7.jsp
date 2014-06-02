<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<script type="text/javascript">
	jQuery(document).ready(function() { 
		upload.highlight("showUploadCmdsInSelectTag");
		jQuery("#uploadTest").hide(); 
	});
</script>

<jsp:include page="FileUpload2014TestInclude.jspf"/>
	
<tf:upload suffices="csv|txt" maxsize="200" uploadCmd="histsim" showUploadCmdsInSelectTag="histsim,tso"/>
The most simple example with only the required 3 attributes (suffices, maxsize (in KB) and uploadCmd).<br/> 
Please note, that you need to have an entry in the DB table <b>"FILE_SCREENER" where UPLOAD_CMD='histsim' and IS_DIRECTORY=1</b>.<br/>
The &lt;tf:upload&gt; will then copy the uploaded file (if it is virus free) into the directory that is given in the DB field 'FILE_SCREENER.PATTERN'.<br/><br/>  
<div class="tagSource">
	Upload Tag:
	<pre>
	&lt;%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %&gt;
	&lt;tf:upload suffices="csv|txt" maxsize="200" uploadCmd="histsim" showUploadCmdsInSelectTag="histsim,tso"/&gt;
	</pre>
</div>
<div class="htmlSource">
	<a href="#"	onclick="upload.toggleHtmlSource()">+ Show generated HTML</a>
	<pre>&nbsp;</pre>
</div>
