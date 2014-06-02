<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<script type="text/javascript">
	jQuery(document).ready(function() { 
		upload.highlight("filenamePrefix");
		jQuery("#uploadTest").hide(); 
	});
</script>

<jsp:include page="FileUpload2014TestInclude.jspf"/>
	
<tf:upload suffices="jpg" maxsize="200" uploadCmd="histsim" 
				   filenamePrefix="${time}"/>
Just like Example 1, but with the attribute 'filenamePrefix'.<br/>
If you drag a file called 'test.txt' on the 'Add files' button, 
this file will be copied as '{filenamePrefix}test.txt' on our server.<br/><br/> 
<div class="tagSource">
	Upload Tag:
	<pre>
	&lt;%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %&gt;
	&lt;tf:upload suffices="jpg" maxsize="200" uploadCmd="histsim" 
				   filenamePrefix="${time}"&gt;
	</pre>
</div>
<div class="htmlSource">
	<a href="#"	onclick="upload.toggleHtmlSource()">+ Show generated HTML</a>
	<pre>&nbsp;</pre>
</div>
