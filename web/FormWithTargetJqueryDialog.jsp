<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
		<link rel="stylesheet" href="//code.jquery.com/ui/1.10.4/themes/smoothness/jquery-ui.css">
		<script src="//code.jquery.com/jquery-1.10.2.js"></script>
		<script src="//code.jquery.com/ui/1.10.4/jquery-ui.js"></script>
		<script type="text/javascript">
			$(function() {
				$("#dialog").dialog({ width: 500, height: 250, maxHeight: 500});
				$("#dialog").dialog("close");
//			$("#dialog").dialog("destroy").remove();
			});

			//http://stackoverflow.com/questions/12662552/submit-a-form-inside-a-jquery-dialog-box
			var intervalId;

			function startPollingResultPage() {
				intervalId = window.setInterval(function() {
					$.post("FormWithTargetServlet", $("#frm").serialize(), function(result) {
						$("#dialog").dialog("open");
						$("#dialog").html(result);
						if (result.indexOf("refresh") === -1) {
							window.clearInterval(intervalId);
							intervalId = 0;
							$("#dialog").dialog({height: 500});
						}
					});
				}, 2000);
			}
		</script>
    </head>
    <body>
		<form id="frm" action="FormWithTargetServlet" method="POST">
			<input name="firstname" id="firstname" type="text" value="Be"/>
			<input name="lastname" id="lastname" type="text" value="Mo"/>
			<input type="submit" value="Los" onclick="startPollingResultPage(); return false;"/>
		</form>

		<div id="dialog" title="Basic dialog">
			<p>Will be filled by JS</p>
		</div>
    </body>
</html>
