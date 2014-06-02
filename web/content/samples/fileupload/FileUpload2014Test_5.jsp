<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<script type="text/javascript">
	jQuery(document).ready(function() {
		upload.highlight("preview");
		jQuery("#uploadTest").hide();
	});
</script>

<jsp:include page="FileUpload2014TestInclude.jspf"/>

<tf:upload suffices="jpg|gif|png" maxsize="200" uploadCmd="histsim"
		   preview="true"
		   filenamePrefix="${time}"
		   multiple="true"
		   javascriptCallback="helloGlobal"
		   javaCallback="com.db.tradefinder.taglib.FileUpload2014Tag.hello"/>
Just like Example 4, but with the attribute 'preview' set to true.<br/>
You can style the file upload the way you want it.<br/><br/>
<div class="tagSource">
	Upload Tag:
	<pre>
	&lt;%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %&gt;
	&lt;tf:upload suffices="jpg|gif|png" maxsize="200" uploadCmd="histsim"
				preview="true"
				filenamePrefix="${time}"
				multiple="true"
				javascriptCallback="helloGlobal"
				javaCallback="com.db.tradefinder.taglib.FileUpload2014Tag.hello"/&gt;
	</pre>
</div>
<div class="htmlSource">
	<a href="#"	onclick="upload.toggleHtmlSource()">+ Show generated HTML</a>
	<pre>&nbsp;</pre>
</div>
