<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<c:set var="daysLen" value="${fn:length(days)}"/>
<c:set var="nz" value="${colHasNonZeroValues}"/>

<!DOCTYPE html>
<html>
    <head>
        <title>Sandbox</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<script type="text/javascript" src="js/amcharts.js"></script>
		<script type="text/javascript" src="js/pie.js"></script>
		<script type="text/javascript" src="js/serial.js"></script>
		<script type="text/javascript" src="tf/common/framework/jquery/jquery-1.7.1.min.js"></script>
		<script type="text/javascript" src="js/jquery.ajaxfileupload.js"></script>
		<script type="text/javascript" src="js/sandbox.js"></script>
		<style type="text/css">
			#sandbox { padding: 0; }
			#sandbox table .right        { text-align: right; }
			#sandbox table .center        { text-align: center; }
			#sandbox table .left        { text-align: left; }

			#sandbox .info { color: #666666; width: 100%; text-align: center; }
			#sandbox #tblSandbox        { display: inline-block; margin: 0 5px 0 8px; }
			#sandbox #tblSandbox td, #tblSandbox th { padding: 2px 8px; }
			#sandbox #tblSandbox td, #sandbox #tblPagination td.legend span.zero { color: #666666; }

			#sandbox #tblSandbox td.bold { font-weight: bold; }

			#sandbox #tblSandbox thead th input { width: 50px; }
			#sandbox #tblSandbox thead th { vertical-align: top; font-weight: bold; }
			#sandbox #tblSandbox thead th.padLeft { padding-left: 10px; }
			#sandbox #tblSandbox thead th.alignMiddle { vertical-align: middle; }

			#sandbox #tblSandbox tbody tr.odd  { background-color: #F7F7F7; }
			#sandbox #tblSandbox tbody tr.even { background-color: white; }
			#sandbox #tblSandbox td.pos, #tblPagination td.legend span.pos { color: #00DB00; }
			#sandbox #tblSandbox td.neg, #tblPagination td.legend span.neg { color: #DB0000; }
			#sandbox #tblSandbox tbody tr.highlight { background-color: #FFD700; }

			#sandbox #tblSandbox .trNoData, #sandbox #tblSandbox .trNoResults { display: none; }
			#sandbox #tblSandbox .trNoData td, #sandbox #tblSandbox .trNoResults td { color: #E00034; font-size: 13px; }

			#sandbox #tblSandbox thead th { padding-left: 10px !important; }

			#sandbox #tblSandbox thead .sorting_asc {
				background-image: url("content/common/jQuery/plugins/dataTables_1.8.2/images/sort_asc.png");
				background-repeat: no-repeat;
			}
			#sandbox #tblSandbox thead .sorting {
				background-image: url("content/common/jQuery/plugins/dataTables_1.8.2/images/sort_both.png");
				background-repeat: no-repeat;
			}
			#sandbox #tblSandbox thead .sorting_desc {
				background-image: url("content/common/jQuery/plugins/dataTables_1.8.2/images/sort_desc.png");
				background-repeat: no-repeat;
			}
			#sandbox #tblSandbox thead .ui-icon-signal {
				background-image: url("tf/common/framework/jquery/css/tf-theme/images/ui-icons_222222_256x240.png");
				background-position: -32px -176px;
				width: 16px; height: 16px;
				display: inline-block;
			}

			#sandbox #linechartdivWrapper { border: 1px solid #CCCCCC; margin: 2px 10px; }
			#sandbox #linechartdiv { width: 100%; height: 400px; }

			#sandbox #barchartdivWrapper { border: 1px solid #CCCCCC; margin: 2px 10px; }
			#sandbox #barchartdiv { width: 100%; height: 400px; }                        

			#sandbox #chartdiv {
				width: 175px; height: 400px;
				display: inline-block;
				vertical-align: top;
				font-size: 11px;
				border: 2px solid #E9F0F6;
				padding: 10px;
				background: linear-gradient(white, #E9F0F6);
				filter: progid:DXImageTransform.Microsoft.gradient(startColorStr='white', EndColorStr='#E9F0F6');
			}
			#sandbox #chartdivInfo {
				height: 100%; color: #333333; padding-top: 60px;
				vertical-align: middle; text-align: center;
				font-size: 17px; font-weight: bold;
				line-height: 28px;
			}
			#sandbox #tblPagination { width: 100%; }
			#tblPagination td.legend span { vertical-align: middle; }
			#tblPagination td.legend span.squareNeg {
				margin-right: 5px; width: 10px; height: 10px; display: inline-block; background-color: #DB0000;
			}
			#tblPagination td.legend span.squarePos {
				margin-right: 5px; width: 10px; height: 10px; display: inline-block; background-color: #00DB00;
			}
			#tblPagination td.legend span.squareZero {
				margin-right: 5px; width: 10px; height: 10px; display: inline-block; background-color: #666666;
			}

			#sandbox .dataTables_paginate span.disabled {
				color: #999999;
			}

			#tblSandbox .dataTables_paginate .first,
			#tblSandbox .dataTables_paginate .previous,
			#tblSandbox .dataTables_paginate .next,
			#tblSandbox .dataTables_paginate .last,
			#sandbox .chart {
				// marker classes for JS
			}

			#sandbox #tblAboveDataTable { width:100%;margin: 10px 0; }

			div.panel {
				background-color: #E9F0F6;
				clear: both;
				padding: 5px 5px 5px 7px;
			}
			div.panel .panelized.hide {
				display: none;
			}
			div.panel.hide {
				display: none;
			}
			div.belowPanel.hide {
				display: none;
			}
			div.panel.closed {
				height: 22px;
			}
			div.panel.transparent {
				background-color: rgba(0, 0, 0, 0);
			}
			div.panel table.panelHeader {
				width: 100%;
			}
			div.panel table.panelHeader td {
				vertical-align: middle;
				white-space: nowrap;
			}
			div.panel table.panelHeader td.filler {
				cursor: pointer;
				font-size: 18px;
				line-height: 22px;
			}
			div.panel table.panelHeader td.header.clickHover {
				color: #0098DB !important;
			}
        </style>

    </head>
    <body>
        <h1>Sandbox</h1>

        <script type="text/javascript">
			jQuery(document).ready(function() {
				var nz = [${nz[0]}, ${nz[1]}, ${nz[2]}, ${nz[3]}, ${nz[4]}, ${nz[5]}, ${nz[6]}];
				hs_sandbox.init(nz, ${jsonForTradingDays});
			});
        </script>

		<div class="glmMainTitle ra">Work in progress</div>

        <div id="sandbox" class="content">
            <div class="panel">
                <%-- Blue header with title 'HistSim VaR SandBox' --%>
				<table class="panelHeader">
                    <tbody>
                        <tr>
                            <td class="title header">HistSim VaR SandBox</td>
                        </tr>
                    </tbody>
                </table>

				<div id="hs_config_panel1" class="panelized" style="margin-top: 10px; clear: both;">
                    <div class="results" id="hs_config_rd1">

						<%-- 'Detailed View' --%>
						<div class="content title" style="text-align: center">Detailed View</div>
						<div class="info">FO_TRADE_ID: ${infoFoTradeId}, VALUATION_CCY: ${infoValCcy}, COB_DATE: ${infoCobDate}, UNIT_CODE: ${infoUnitCode}</div>
						<div style="text-align: center">
							<c:set var="daysLen" value="${fn:length(days)}"/>
							${daysLen} days from ${days[daysLen-1].date} to ${days[0].date}
						</div>

						<div id="hs_histsimVar_tabs" class="content ui-tabs ui-widget ui-widget-content ui-corner-all">
							<ul class="hs ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
								<li class="ui-state-default ui-corner-top">
									<a href="#summaryDiv">Summary</a>
								</li>
								<li class="ui-state-default ui-corner-top">
									<a href="#uploadDiv">Upload</a>
								</li>
							</ul>
						</div>

						<div id="summaryDiv">
							<div id="linechartdivWrapper">
								<a href="#" onclick="hs_sandbox.toggleLinechart()">Hide Linechart</a>
								<div id="linechartdiv">
									<%-- linechart will be inserted here --%>
								</div>
							</div>
							<div id="barchartdivWrapper">
								<a href="#" onclick="hs_sandbox.toggleBarchart()">Hide Barchart</a>                                                                
								<div id="barchartdiv">
									<%-- barchart will be inserted here --%>
								</div>
							</div>

							<%-- dropdown for number of entries, dropdown for selecting the month --%>
							<table id="tblAboveDataTable" style="float: left">
								<tbody>
									<tr>
										<td class="content">
											Show
											<select id="selectEntriesPerPage">
												<option value="10">10</option>
												<option value="25" selected="selected">25</option>
												<option value="50">50</option>
												<option value="100">100</option>
											</select>
											entries
										</td>
										<td class="header">
											Month&nbsp;
											<select id="selectMonth">
												<option value="" selected="selected">All</option>
												<c:forEach var="month" items="${distinctMonths}">
													<option value="${month}">${month}</option>
												</c:forEach>
											</select>
											<br>
										</td>
									</tr>
								</tbody>
							</table>

							<%-- Table with HistSim VaR SandBox data --%>
							<table id="tblSandbox" style="clear: both">
								<thead>
									<tr>
										<c:set var="cssTh" value="header right padLeft"/>
										<th class="header alignMiddle sorting" data-sortcol="date">Date</th>
										<th class="${cssTh} ${colHasNonZeroValues[1] ? ' sorting' : ''}" data-sortcol="cr">
											CURVE<br>Residual<br>
											<c:if test="${colHasNonZeroValues[0]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtCR" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="${cssTh} ${colHasNonZeroValues[1] ? ' sorting' : ''}" data-sortcol="csd">
											CURVE<br>SwapDelta<br>
											<c:if test="${colHasNonZeroValues[1]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtCS" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="${cssTh} ${colHasNonZeroValues[2] ? ' sorting' : ''}" data-sortcol="csg">
											CURVE<br>SwapGamma<br>
											<c:if test="${colHasNonZeroValues[2]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtSG" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="${cssTh} ${colHasNonZeroValues[3] ? ' sorting' : ''}" data-sortcol="ii">
											IRVOL<br>IRVega<br>
											<c:if test="${colHasNonZeroValues[3]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtII" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="${cssTh} ${colHasNonZeroValues[4] ? ' sorting' : ''}" data-sortcol="ir">
											IRVOL<br>Residual<br>
											<c:if test="${colHasNonZeroValues[4]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtIR" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="${cssTh} ${colHasNonZeroValues[5] ? ' sorting' : ''}" data-sortcol="nn">
											NET<br>NET<br>
											<c:if test="${colHasNonZeroValues[5]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtNN" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="${cssTh} ${colHasNonZeroValues[6] ? ' sorting' : ''}" data-sortcol="rr">
											Residual<br>Residual<br>
											<c:if test="${colHasNonZeroValues[6]}">
												<span class="ui-icon-signal chart">&nbsp;</span>
												<input id="txtRR" type="text" title="Example: >0.5, <0.5, =1.0, 1.0"/>
											</c:if>
										</th>
										<th class="header">Actions</th>
									</tr>
								</thead>
								<tbody>
									<tr class="trNoData"><%-- This line will be made visible by JS, when no data is available --%>
										<td class="left" colspan="9">No data available. No data available.</td>
									</tr>
									<tr class="trNoResults"><%-- No Results after filtering --%>
										<td class="left" colspan="9">No results.</td>
									</tr>
									<%-- The content will be filled by Javascript. It uses the JSON object tradingDays for that --%>
								</tbody>
							</table>

							<%-- Placeholder for AmChart right to the Sandbox data table --%>
							<div id="chartdiv">
								<%-- Content will be inserted dynamically with AmChart --%>
								<div id="chartdivInfo">
									Click on the icon<br>
									<img src="images/riskMonitor/chartTypes/piechart.png" alt="Chart"><br>
									to display a chart<br>
									of the current day
								</div>
							</div>
                        </div><%-- summaryDiv --%>

						<div id="uploadDiv">
							<iframe frameborder="0" width="450" height="40"
									src="/service/tradeFinder/tradefnd/content/riskMgmt/histsim/jsp/HistsimVarUploadForm.jsp">
							No IFrames
							</iframe>
						</div>

                    </div><%-- results --%>

					<%-- Pagination area (Entries from ... to, First/Prev/Next/Last-Buttons --%>
					<table id="tblPagination">
						<tr>
							<td class="left">
								Showing <span id="entriesFromTo">1 to 25</span> of <span id="entriesCount">34</span> entries
							</td>
							<td class="center legend">
								<span class="squareNeg">&nbsp;</span><span class="neg">Value &lt; 0.01</span>
								<span class="squareZero">&nbsp;</span><span class="zero">0</span>
								<span class="squarePos">&nbsp;</span><span class="pos">Value &gt; 0.01</span>
							</td>
							<td class="right">
								<%-- Pagination --%>
								<div class="dataTables_paginate" id="hs_sandbox_paginate">
									<span class="first" id="hs_sandbox_first">First</span>
									<span class="previous" id="hs_sandbox_previous">Previous</span>
									<span></span>
									<span class="next" id="hs_sandbox_next">Next</span>
									<span class="last" id="hs_sandbox_last">Last</span>
								</div>
							</td>
						</tr>
					</table>

                </div><!-- hs_config_panel1 -->
            </div><!-- panel -->
        </div><!-- sandbox --> 
    </body>

</html>
