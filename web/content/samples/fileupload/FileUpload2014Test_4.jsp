<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.Date, java.text.SimpleDateFormat" %>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="time"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>

<script type="text/javascript">
	jQuery(document).ready(function() { 
		upload.highlight("multiple"); upload.highlight("label"); 
		jQuery("#uploadTest").hide(); 
	});
</script>

<jsp:include page="FileUpload2014TestInclude.jspf"/>
	
<tf:upload suffices="csv|txt" maxsize="200" uploadCmd="histsim" 
				   label="Your own text to add a file"
				   filenamePrefix="${time}"
				   multiple="true"
				   javascriptCallback="helloGlobal" 
				   javaCallback="com.db.tradefinder.taglib.FileUpload2014Tag.hello"/>
Just like Example 3, but with the attributes 'multiple' set to true and 'label' set to 'Your own text ...'.<br/>
You can now drag multiple files on the 'Add files' button, the javascriptCallback will be called multiple times.<br/><br/> 
<div class="tagSource">
	Upload Tag:
	<pre class="prettyprint">
	&lt;%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder" %&gt;
	&lt;tf:upload suffices="csv|txt" maxsize="200" uploadCmd="histsim" 
				   label="Your own text to add a file"
				   filenamePrefix="${time}"
				   multiple="true"
				   javascriptCallback="helloGlobal" 
				   javaCallback="com.db.tradefinder.taglib.FileUpload2014Tag.hello"/&gt;
	</pre>
</div>
<div class="htmlSource">
	<a href="#"	onclick="upload.toggleHtmlSource()">+ Show generated HTML</a>
	<pre class="prettyprint">&nbsp;</pre>
</div>
