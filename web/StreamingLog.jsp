<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Log Viewer</title>
		<style type="text/css">
			.body { font-family: sans-serif; font-size: 10px; }
			#logHeader	{ width: 100%; }
			#logHeader tr td { padding: 5px; }
			#logHeader input[type=text] { width: 50px; display: inline; }
			#logHeader #txtTerm, #logHeader #txtFilter { width: 150px; }
			#logHeader #spanMark .count { color: lightgrey; }
			.log 	{	width: 100%; height: 500px; overflow-y: scroll;
					font-family: monospace; font-size: 12px; }
			.log .error, #filter .error { color: #e00034; }
			.log .warn,	 #filter .warn  { color: #ffa100; }
			.log .info,  #filter .info  { color: #7ab800; }
			.log .highlight { background-color: lightgrey; }
			#filter .error, #filter .warn, #filter .info { font-weight: bold; }
			div#filter, div#filter .btn-group { display: inline-block; }
			#filter .text { color: black; }
			.log .exc   { color: #e00034; font-weight: bold; }
		</style>
		<link href="resources/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
		<script type="text/javascript" src="js/jquery-1.11.1.js"></script>
		<script type="text/javascript" src="resources/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="js/streamingLog.js"></script> 
	</head>
	<body>
		<h2>Log Viewer</h2>

		<ul class="nav nav-tabs" role="tablist">
			<li class="active" id="tabCatalina"><a href="#catalina" role="tab" data-toggle="tab">catalina.out</a></li>
			<li id="tabTomcatBase"><a href="#tomcat_base" role="tab" data-toggle="tab">tomcat_base.log</a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="catalina">
				<div class="panel panel-default">
					<div class="panel-heading">
						<table id="logHeader">
							<tr>
								<td>
									<button id="btnUpdateLog" class="btn btn-primary" onclick="updateLog()">Tail</button>
									<input id="txtLines" type="text" value="500" class="form-control"/> lines
									<span id="btnsPauseClear">
										<button id="btnPauseLog" class="btn btn-primary" onclick="pauseLog()" disabled="disabled">
											Pause
										</button>
										<button id="clearLog" class="btn btn-danger" onclick="clearLog()">Clear</button>
									</span>
								</td>
								<td>
									<span id="find">Poll: every 
										<input id="txtPollInterval" type="text" value="1" class="form-control"/>s</span>
								</td>
							</tr>
							<tr>
								<td>
									<span id="spanMark">Mark text:
										<input id="txtTerm" type="text" placeholder="Ex. com.db.tradefinder" class="form-control"/>
										<button id="btnMark" class="btn btn-primary" onclick="mark()" title="Search for text">Mark</button>
										<button id="btnMarkRegex" class="btn btn-info" onclick="markRegex()" title="Search for Regex">Regex</button>
										<span class="count">&nbsp;</span>
									</span>
								</td>
								<td>
									<div id="filter">
										Filter lines: 
										<div class="btn-group">
											<button class="btn btn-default error" onclick="filter(this)" data-toggle="tooltip" 
													data-placement="top" title="Show lines with error messages">E</button>
											<button class="btn btn-default warn"  onclick="filter(this)" data-toggle="tooltip" 
													data-placement="top" title="Show lines with warning messages">W</button>
											<button class="btn btn-default info"  onclick="filter(this)" data-toggle="tooltip" 
													data-placement="top" title="Show lines with info messages">I</button>
										</div>
										
										<button class="btn btn-default text"  onclick="filter(this)" data-toggle="tooltip" 
												data-placement="top" title="Show lines with this text">T</button>
										<input id="txtFilter" type="text" placeholder="Ex. msgFromYourClass=" value="" class="form-control"/>
										<span class="count">&nbsp;</span>
									</div>
								</td>
							</tr>
						</table>
					</div>
					<div class="panel-body">
						<div class="log">
							Press the button 'Tail' to see the last lines of your log file.
						</div>
					</div>
				</div>
			</div>
			<div class="tab-pane active" id="tomcat_base">
				tomcat_base.log
			</div>
		</div>
	</body>
</html>