<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>

<%-- 
    Document   : StreamingLogHeader
    Created on : 30.10.2014, 09:35:53
    Author     : Bernhard
--%>
<%--
<!DOCTYPE html> 
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Log Viewer</title>
		<link href="resources/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
		<link href="resources/css/streamingLog.css" rel="stylesheet" type="text/css"/>
		<script type="text/javascript" src="js/jquery-1.11.1.js"></script>
		<script type="text/javascript" src="resources/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="resources/js/streamingLog.js"></script> 
	</head>
	<body>
--%>
				<div class="panel panel-default" id="${param.file}">
					<div class="panel-heading">
						<table class="logHeader">
							<tr>
								<td>
									<span class="spanTail">Tail: </span>
									<input type="text" value="500" class="txtLines form-control"/> lines
									<span class="btnsPauseClear">
										<button class="btnUpdateLog btn btn-primary" onclick="updateLog()">Tail</button>
										<button class="btnPauseLog btn btn-primary" onclick="pauseLog()" disabled="disabled">
											Pause
										</button>
										<button class="btnClearLog btn btn-danger" onclick="clearLog()">Clear</button>
									</span>
								</td>
								<td>
									<span class="spanPoll">Poll: every </span>
									<input type="text" value="1" class="txtPollInterval form-control"/>s
								</td>
								<td class="logInfo">
									&nbsp;
								</td>
							</tr>
							<tr>
								<td class="tdMark">
									<span class="spanMark">Mark text:</span>
									<input class="txtTerm form-control" type="text" placeholder="Ex. com.db.tradefinder"/>
									<button class="btnMark btn btn-primary" onclick="mark()" title="Search for text">Mark</button>
									<button class="btnMarkRegex btn btn-info" onclick="markRegex()" title="Search for Regex">Regex</button>
									<span class="count">&nbsp;</span>
								</td>
								<td class="tdFilter" colspan="2">
									<span class="spanFilter">Filter lines: </span>
									<div class="btn-group">
										<button class="btn btn-default all" onclick="filter(this)" data-toggle="tooltip" 
												data-placement="top" title="Show all lines">All</button>
										<button class="btn btn-default error" onclick="filter(this)" data-toggle="tooltip" 
												data-placement="top" title="Show lines with error messages">E</button>
										<button class="btn btn-default warn"  onclick="filter(this)" data-toggle="tooltip" 
												data-placement="top" title="Show lines with warning messages">W</button>
										<button class="btn btn-default info"  onclick="filter(this)" data-toggle="tooltip" 
												data-placement="top" title="Show lines with info messages">I</button>
									</div>

									<button class="btn btn-default text"  onclick="filter(this)" data-toggle="tooltip" 
											data-placement="top" title="Show lines with this text">T</button>
									<input type="text" placeholder="Ex. msgFromYourClass=" value="" class="txtFilter form-control"/>
									<span class="count">&nbsp;</span>
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
<%--					
	</body>
</html>
--%>