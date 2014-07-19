<%--
    Document   : batchUser
    Created on : 17.05.2014, 19:53:58
    Author     : Bernhard
--%>

<%-- einzeiliger JSP-Kommentar --%>

<%
	int dummy = 0;
	// das ist ein Kommentar
	// Der FileUsageScanner sollte die Kommentare rausschneiden
	/**
	 * Übrig bleibt dann nur noch dummy...
	 */
	dummy = 1;
	/** Einzeiliger Kommentar*/
	dummy = 2;
	// nochmal
	dummy = 3;
%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tf" uri="http://tradefinder.db.com/tags/tradefinder"%>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<c:set var="date"><%=new SimpleDateFormat("HHmmss").format(new Date())%></c:set>
		<style>
			#tblBatchUserPreview td, #tblBatchUserHeaders th { text-align: left; }
			#tblBatchUserPreview tbody tr.even { background-color: #EEEEEE; }
			#tblBatchUserPreview tbody tr.odd  { background-color: white; }
			#tblBatchUserPreview tr td {word-wrap: break-word; width: 100px; }
		</style>
        <script type="text/javascript" src="resources/js/batchUser.js"></script>

		<tf:upload filenamePrefix="bm_${date}_" maxsize="100" suffices="csv|zip" uploadCmd="batchUser"
                   javascriptCallback="batchUser.unzip" label="Lock/Unlock Users (CSV or ZIP file)"/>
		<input type="button" onclick="batchUser.previewCsv()" value="Preview"/>
		<div id="batchUserdialog" title="Users that will be locked">
			<b>Number of users: </b><span id="numberOfUsers">&nbsp;</span><br/><br/>
			<table id="tblBatchUserHeaders">
				<tr>
					<th>Account</th>
					<th>Action</th>
					<th>Reason</th>
					<th>RequestNo</th>
					<th>Date</th>
				</tr>
			</table>
			<div id="tblWrapper" style="height: 340px; overflow-y: auto; overflow-x: hidden">
				<table id="tblBatchUserPreview">
					<tbody>
						<tr><td>&nbsp;</td></tr>
					</tbody>
				</table>
			</div>
			<br/>
			<div id="btnArea" style="text-align: right">
				<input type="button" onclick="batchUser.lockUsers()" value="Lock users"/>
				<input type="button" onclick="jQuery('#batchUserdialog').dialog('close');" value="Cancel"/>
			</div>
		</div>
