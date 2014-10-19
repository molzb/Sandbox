<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>File Usage</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script type="text/javascript" src="js/jquery-1.11.1.js"></script>
		<script type="text/javascript" src="js/dirwalk.js"></script>
		<script type="text/javascript" src="resources/jstree/jstree.min.js"></script>
		<link href="resources/css/bootstrap.min.css" type="text/css" rel="stylesheet">
		<link href="resources/jstree/themes/default/style.css" type="text/css" rel="stylesheet">
		<link href="resources/css/dirwalk.css" type="text/css" rel="stylesheet">
	</head>
	<body>
		<div id="content">

			<div id="searchFile">
				<input id="txtSearch" type="text" placeholder="Ex. abc.jsp OR *abc.jsp OR abc.*" 
					   autofocus onkeydown="dirwalk.searchFile($(this).val())"/>
				&nbsp;
				<img src="images/view.png" alt="Search file" onclick="dirwalk.searchFile($('#txtSearch').val())"/>
			</div>
			<div id="tree">
				${list}
			</div>
			<div id="details">
				<h2 id="filename">Click a file in the tree on the left</h2>
				<h3 id="type">Type: </h3>
				<h3 id="location">Location: </h3>
				<h3 id="size">Size: </h3>
				<h3 id="count">#Count </h3>
				<table class="table table-striped table-bordered table-hover">
					<thead>
						<tr>
							<th colspan="4">File</th>
							<th colspan="6">References</th>
						</tr>
						<tr>
							<th>Name+Status</th>
							<th>#Lines</th>
							<th>Last modif.</th>
							<th>Pagehits</th>
							
							<th>Java</th>
							<th>JSP</th>
							<th>Spring</th>
							<th>JAR</th>
							<th>TF_MENU</th>
							<th>Rest</th>
						</tr>
					</thead>
					<tr><td colspan="10">Please click a file</td></tr>
				</table>
			</div>
		</div>
	</body>
</html>
