"use strict";
/*global jQuery, alert, console, AmCharts */

/** Immutable array that is filled by the JAVA controller */
var allTradingDays = [];
/** Dynamic array that is filled with (filtered/sorted/paginated) values from the original array {allTradingDays} */
var tradingDays = [];

var entriesPerPage = 25;
/** Pagination: Used in pagination info: 'Showing [entriesFrom] to [entriesTo] of 25 entries' */
var entriesFrom, entriesTo;
/** Pagination: current page (starting with 0)*/
var currentPage = 0;
/** Pagination: last page (starting with 0). Example: 26 entries, 25 entries per page, maxPage = 1 */
var maxPage = 0;
/** e.g. Aug, Feb; "" for no filter */
var filterMonth = "";
/** e.g. >-1, <0.5, =0 */
var filter = "";
/** Column in table which gets filtered */
var filterCol = -1;
/** Filter expression, e.g. >-1, is used togethter with filterCol */
var filterExpr = "";
/** Valid sortcols: 'date' (column 1), 'csr' (column 2), 'csd' (...), 'ii', 'ir', 'nn', 'rr' */
var sortCol = "";
/** Flag for the sort order (in conjunction with sortCol): 'asc' or 'desc' */
var sortOrder;
var nonZeroColumns;

var linechart;
var linechartData = [];
var barchart;


function consoleLog(s) {
	if ('console' in self && 'log' in console) {
		console.log(s);
	}
}

var hs_sandbox = {
	init: function(nz, jsonForTradingDays) {
		nonZeroColumns = nz;
		allTradingDays = jsonForTradingDays;
		tradingDays = JSON.parse(JSON.stringify(allTradingDays));

		jQuery("#selectEntriesPerPage").val(entriesPerPage);
		jQuery("#selectMonth").val(filterMonth);

		hs_sandbox.initFilterEvents();
		// table highlighting
		jQuery("#tblSandbox tbody tr").mouseover(function() {
			jQuery(this).addClass("highlight");
		});
		jQuery("#tblSandbox tbody tr").mouseout(function() {
			jQuery(this).removeClass("highlight");
		});

		// select fields (month and entries per page
		jQuery("#selectMonth").change(function() {
			hs_sandbox.filterMonth();
		});

		jQuery("#selectEntriesPerPage").change(function() {
			entriesPerPage = jQuery(this).val();
			hs_sandbox.buildTableRows();
		});

		// sorting in table headers
		jQuery("#tblSandbox th.header.sorting, #tblSandbox th.header.sorting_asc, #tblSandbox th.header.sorting_desc").click(function() {
			if (jQuery(this).hasClass("sorting")) {
				jQuery(this).removeClass("sorting").addClass("sorting_asc");
				consoleLog("jquery(this).data(sortcol)=" + jQuery(this).data("sortcol"));
				hs_sandbox.sortColumn(jQuery(this).data("sortcol"), "asc");
			}
			else if (jQuery(this).hasClass("sorting_asc")) {
				jQuery(this).removeClass("sorting_asc").addClass("sorting_desc");
				hs_sandbox.sortColumn(jQuery(this).data("sortcol"), "desc");
			}
			else if (jQuery(this).hasClass("sorting_desc")) {
				jQuery(this).removeClass("sorting_desc").addClass("sorting_asc");
				hs_sandbox.sortColumn(jQuery(this).data("sortcol"), "asc");
			}
			jQuery(this).nextAll(".sorting_asc,.sorting_desc").removeClass("sorting_asc sorting_desc").addClass("sorting");
			jQuery(this).prevAll(".sorting_asc,.sorting_desc").removeClass("sorting_asc sorting_desc").addClass("sorting");
		});

		hs_sandbox.buildTableRows();

		hs_sandbox.getLinechartData();
		hs_sandbox.makeLinechart();
		hs_sandbox.makeBarchart();

		jQuery(".tipsy").tipsy({gravity: "w"});
	},
	toggleLinechart: function() {
		var linechart = jQuery("#linechartdiv");
		linechart.toggle();
		jQuery("#linechartdivWrapper a").text(linechart.is(':visible') ? 'Hide Chart' : 'Show Chart');
	},
	toggleBarchart: function() {
		var barchart = jQuery("#barchartdiv");
		barchart.toggle();
		jQuery("#barchartdivWrapper a").text(linechart.is(':visible') ? 'Hide Chart' : 'Show Chart');
	},
	/**
	 * set events for the filters in the table header of #tblSandbox
	 * @returns {void}
	 */
	initFilterEvents: function() {
		// prevent event bubbling in filter (clicking into input field should not sort the column)
		jQuery("#txtCR,#txtCS,#txtSG,#txtII,#txtIR,#txtNN,#txtRR").click(function() {
			return false;
		});

		// prevent forbidden characters in filter in table header columns
		jQuery("#txtCR,#txtCS,#txtSG,#txtII,#txtIR,#txtNN,#txtRR").keydown(function(e) {
			if (!hs_sandbox.allowedKey(e.keyCode)) {
				consoleLog(e.keyCode + "prevented");
				e.preventDefault();
				return false;
			}
		});
		// filtering in table header columns
		jQuery("#txtCR").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 1);
		});
		jQuery("#txtCS").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 2);
		});
		jQuery("#txtSG").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 3);
		});
		jQuery("#txtII").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 4);
		});
		jQuery("#txtIR").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 5);
		});
		jQuery("#txtNN").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 6);
		});
		jQuery("#txtRR").keyup(function() {
			hs_sandbox.filterCol(jQuery(this).val(), 7);
		});
	},
	setOddEvenTableRows: function() {
		jQuery("#tblSandbox tbody tr").removeClass("odd").removeClass("even");
		jQuery("#tblSandbox tbody tr:visible:odd").addClass("odd");
		jQuery("#tblSandbox tbody tr:visible:even").addClass("even");
	},
	setPaginationButtons: function() {
		jQuery(".dataTables_paginate span").removeClass("disabled");
		jQuery(".dataTables_paginate span").off('click');
		consoleLog("currentPage=" + currentPage + ", maxPage=" + maxPage);
		if (currentPage !== 0) {
			jQuery(".dataTables_paginate .first").click(function() {
				currentPage = 0;
				hs_sandbox.buildTableRows();
			});
		} else {
			jQuery(".dataTables_paginate .first").addClass("disabled");
		}

		if (currentPage - 1 >= 0) {
			jQuery(".dataTables_paginate .previous").click(function() {
				currentPage--;
				hs_sandbox.buildTableRows();
			});
		} else {
			jQuery(".dataTables_paginate .previous").addClass("disabled");
		}

		if (currentPage + 1 <= maxPage) {
			jQuery(".dataTables_paginate .next").click(function() {
				currentPage++;
				hs_sandbox.buildTableRows();
			});
		} else {
			jQuery(".dataTables_paginate .next").addClass("disabled");
		}

		if (currentPage !== maxPage) {
			jQuery(".dataTables_paginate .last").click(function() {
				currentPage = maxPage;
				hs_sandbox.buildTableRows();
			});
		} else {
			jQuery(".dataTables_paginate .last").addClass("disabled");
		}
	},
	sortColumn: function(col, order) {
		consoleLog("col to sort=" + col + ", sortorder=" + sortOrder);
		sortCol = col;
		sortOrder = order;
		hs_sandbox.buildTableRows();
	},
	filterMonth: function() {
		filterMonth = jQuery("#selectMonth").val();
		hs_sandbox.buildTableRows();
	},
	filterCol: function(st, col) {
		var char1 = st.charAt(0);
		var lastCh = st.length > 0 ? st.charAt(st.length-1) : "";
		consoleLog("filterCol " + st + "/" + col);
		if ( (lastCh === ">" || lastCh === "<" || lastCh === "." || lastCh === "=" || lastCh === ".") ||
			 ((char1 === "<" || char1 === ">" || char1 === "=") && st.length === 1) ) {
			filterCol = -1;
			consoleLog("filterCol return");
			return;
		}
		jQuery("#tblSandbox .trNoResults").hide();	// when sth. was displayed before that
		filterCol = col;
		filterExpr = st;
		hs_sandbox.buildTableRows();
	},
	matches: function(searchExpr, tdText) {
		var tdFloat = parseFloat(tdText);
		var eq = false, gt = false, lt = false;

		var searchFirstChar = searchExpr.charAt(0);
		if (searchFirstChar === '>') {
			gt = true;
			searchExpr = searchExpr.substr(1, 99);
		} else if (searchFirstChar === '<') {
			lt = true;
			searchExpr = searchExpr.substr(1, 99);
		} else if (searchFirstChar === '=') {
			eq = true;
			searchExpr = searchExpr.substr(1, 99);
		} else {
			eq = true;
		}

		var searchFloat = parseFloat(searchExpr);
		if (gt === true) {
			return tdFloat > searchFloat;
		} 
		if (lt === true) {
			return tdFloat < searchFloat;
		} 
		if (eq === true) {
			return tdFloat === searchFloat;
		}
	},
	/**
	 * Allowed keys: [0-9.<>-=Shift], Codes 48-57|190|60|16+60|173|16+48
	 * @param c keyChar
	 */
	allowedKey: function(c) {
		return  (c >= 48 && c <= 57) || /** Zahlen */
				(c === 190 || c === 60 || c === 173 || c === 16) || /** Period, Comma, Equals */
				(c === 8 || c === 37 || c === 39) /** Cursor keys */;
	},
	makePieChart: function(dataProvider) {
		// PIE CHART
		jQuery("#chartdiv").text("");
		var chart = new AmCharts.AmPieChart();
		chart.dataProvider = dataProvider;
		chart.titleField = "name";
		chart.valueField = "value";
		chart.startDuration = 0;
		chart.labelsEnabled = false;
		chart.fontSize = 10;

		// WRITE
		chart.addLegend(new AmCharts.AmLegend());
		chart.write("chartdiv");
		return false;
	},
	makeLinechart: function() {
		// SERIAL CHART    
		linechart = new AmCharts.AmSerialChart();
		linechart.pathToImages = "content/common/components/amcharts_js/images/";
		linechart.zoomOutButton = {
			backgroundColor: '#000000',
			backgroundAlpha: 0.15
		};
		linechart.dataProvider = linechartData;
		linechart.categoryField = "date";

		// listen for "dataUpdated" event (fired when chart is inited) and call zoomChart method when it happens
		linechart.addListener("dataUpdated", hs_sandbox.zoomLinechart);

		// AXES category                
		var categoryAxis = linechart.categoryAxis;
		categoryAxis.parseDates = true; // as our data is date-based, we set parseDates to true
		categoryAxis.minPeriod = "DD"; // our data is daily, so we set minPeriod to DD
		categoryAxis.dashLength = 2;
		categoryAxis.gridAlpha = 0.15;
		categoryAxis.axisColor = "#DADADA";

		// first value axis (on the left)
		var valuesAxis = new AmCharts.ValueAxis();
		valuesAxis.axisColor = "#FF6600";
		valuesAxis.axisThickness = 2;
		valuesAxis.gridAlpha = 0;
		linechart.addValueAxis(valuesAxis);

		// GRAPHS
		var day0 = tradingDays[0];
		var graphVals = ["cr", "csd", "csg", "ii", "ir", "nn", "rr"];
		var bullets = ["round", "round", "round", "triangleUp", "triangleUp", "square", "square"];
		var colors = ["#ff0f00", "#ff6600", "#ff9e01", "#fcd202", "#fff801", "#b0de09", "#04D215"];
		for (var i = 0; i < day0.values.length; i++) {
			var graph = new AmCharts.AmGraph();
			graph.valueAxis = valuesAxis; // we have to indicate which value axis should be used
			graph.title = day0.values[i].name;
			graph.valueField = graphVals[i];
			graph.bullet = bullets[i];
			graph.lineColor = colors[i];
			graph.hideBulletsCount = 50;
			linechart.addGraph(graph);
		}

		// CURSOR
		var chartCursor = new AmCharts.ChartCursor();
		chartCursor.cursorPosition = "mouse";
		linechart.addChartCursor(chartCursor);

		// SCROLLBAR
		var chartScrollbar = new AmCharts.ChartScrollbar();
		linechart.addChartScrollbar(chartScrollbar);

		// LEGEND
		var legend = new AmCharts.AmLegend();
		legend.marginLeft = 110;
		linechart.addLegend(legend);

		linechart.addListener('zoomed', function() {});

		// WRITE
		linechart.write("linechartdiv");
	},
	makeBarchart: function() {
		var barchartData = []; /*[{
				country: "USA",
				visits: 4025
			}, {
				country: "China",
				visits: 1882
			}, {
				country: "Japan",
				visits: 1809
			}, {
				country: "Germany",
				visits: 1322
			}];*/
		
		var graphVals = ["cr", "csd", "csg", "ii", "ir", "nn", "rr"];
		var cnt = 0;
		for (var n = 0; n < tradingDays.length; n++) {
			var day = tradingDays[n];
			for (var i = 0; i < day.values.length; i++) {
				barchartData[cnt] = { label: graphVals[i], value: day.values[i].value };
//				barchartData[cnt].label = graphVals[i];
//				barchartData[cnt].value = day.values[i];
				cnt++;
			}
		}
		// SERIAL CHART
		barchart = new AmCharts.AmSerialChart();
		barchart.dataProvider = barchartData;
		barchart.categoryField = "label";
		barchart.sequencedAnimation = false;

		// AXES
		// category
		var categoryAxis = barchart.categoryAxis;
		categoryAxis.labelRotation = 90;
		categoryAxis.gridPosition = "start";
		// value
		// in case you don't want to change default settings of value axis,
		// you don't need to create it, as one value axis is created automatically.
		// GRAPH
		var graph = new AmCharts.AmGraph();
		graph.valueField = "value";
		graph.balloonText = "[[category]]: [[value]]";
		graph.type = "column";
		graph.lineAlpha = 0;
		graph.fillAlphas = 0.8;
		barchart.addGraph(graph);
		barchart.write("barchartdiv");
	},
	// generate some random data, quite different range
	getLinechartData: function() {
		sortOrder = "asc";
		tradingDays.sort(function(row1, row2) {
			return hs_sandbox.compareNum(row1.dateLong, row2.dateLong);
		});

		for (var i = 0; i < tradingDays.length; i++) {
			var day = tradingDays[i];
			var newDate = new Date(day.dateLong);

			var cr = day.values[0].value;
			var csd = day.values[1].value;
			var csg = day.values[2].value;
			var ii = day.values[3].value;
			var ir = day.values[4].value;
			var nn = day.values[5].value;
			var rr = day.values[6].value;

			linechartData.push({
				date: newDate,
				cr: cr,
				csd: csd,
				csg: csg,
				ii: ii,
				ir: ir,
				nn: nn,
				rr: rr
			});
		}
	},
	// this method is called when chart is first inited as we listen for "dataUpdated" event
	zoomLinechart: function(start, end) {
		// different zoom methods can be used - zoomToIndexes, zoomToDates, zoomToCategoryValues
		linechart.zoomToIndexes(tradingDays.length - 50, tradingDays.length);
	},
	setEntriesFromTo: function() {
		if (tradingDays.length > 0) {
			entriesFrom = currentPage * entriesPerPage;
			entriesTo = Math.min(entriesFrom + entriesPerPage, tradingDays.length);
			jQuery("#entriesFromTo").text((entriesFrom + 1) + " to " + entriesTo);
			jQuery("#entriesCount").text(tradingDays.length);
		} else {
			jQuery("#entriesFromTo").text("0");
			jQuery("#entriesCount").text("0");
		}
	},
	/**
	 * Set enabled and disabled elements in the [select] selectEntriesPerPage,<br>
	 * where you can change the entries per page. If you have 20 entries, <br>
	 * the options for [10] and [25] entries are enabled, the options for [50] and [100] are disabled
	 * @param {int} entriesCount
	 * @returns {void}
	 */
	setEntriesPerPage: function(entriesCount) {
		var maxSelectableEntries = 0;
		jQuery("#selectEntriesPerPage option").each(function() {
			maxSelectableEntries = parseInt(jQuery(this).val(), 10);
			if (maxSelectableEntries > entriesCount) {
				return false;
			}
		});
		jQuery("#selectEntriesPerPage option").each(function() {
			if (parseInt(jQuery(this).val(), 10) > maxSelectableEntries) {
				jQuery(this).attr("disabled", "true");
			} else {
				jQuery(this).removeAttr("disabled");
			}
		});
	},
	compareNum: function(n1, n2) {
		if (n1 === n2) {
			return 0;
		}
		if (sortOrder === "asc") {
			return n1 > n2 ? 1 : -1;
		} else {
			return n1 > n2 ? -1 : 1;
		}
	},
	/**
	 * (re)generated the whole table body content of #tblSandbox to apply filtering, sorting, etc.
	 * @returns {tbody}
	 */
	buildTableRows: function() {
		var day, i;
		tradingDays = [];

		if (filterMonth !== "") {
			for (i = 0; i < allTradingDays.length; i++) {
				day = allTradingDays[i];
				if (day.date.indexOf(filterMonth) > -1) {
					tradingDays.push(JSON.parse(JSON.stringify(day)));
				}
			}
		} else {
			tradingDays = JSON.parse(JSON.stringify(allTradingDays));
		}
		
		// filter column, e.g. column 3 gets filtered by the expr '>0.5'
		if (filterCol > -1 && filterExpr !== "" && filterExpr.length >= 1) {
			var filteredTradingDays = [];
			for (i = 0; i < tradingDays.length; i++) {
				day = tradingDays[i];
				if (hs_sandbox.matches(filterExpr, day.values[filterCol-1].value) ) {
					filteredTradingDays.push(JSON.parse(JSON.stringify(day)));
				}
			}
			tradingDays = JSON.parse(JSON.stringify(filteredTradingDays));
			consoleLog("#tradingDays after filter=" + tradingDays.length);
		}

		if (sortCol !== "") {
			switch (sortCol) {
				case "date":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.dateLong, row2.dateLong);
					});
					break;
				case "cr":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[0].value, row2.values[0].value);
					});
					break;
				case "csd":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[1].value, row2.values[1].value);
					});
					break;
				case "csg":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[2].value, row2.values[2].value);
					});
					break;
				case "ii":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[3].value, row2.values[3].value);
					});
					break;
				case "ir":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[4].value, row2.values[4].value);
					});
					break;
				case "nn":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[5].value, row2.values[5].value);
					});
					break;
				case "rr":
					tradingDays.sort(function(row1, row2) {
						return hs_sandbox.compareNum(row1.values[6].value, row2.values[6].value);
					});
					break;
				default:
					consoleLog("sort column [" + sortCol + "] is not implemented");
					break;
			}
		}

		maxPage = parseInt(tradingDays.length / entriesPerPage, 10);
		hs_sandbox.setEntriesFromTo();
		consoleLog("entriesFrom/To=" + entriesFrom + "/" + entriesTo);

		var trs = "";

		for (i = entriesFrom; i < entriesTo; i++) {
			day = tradingDays[i];
			if (day === undefined) {
				break;
			}
			var dayId = day.id;
			var val = day.values;

			var css0 = "right " + val[0].css + (val[0].max || val[0].min ? " bold" : "");
			var css1 = "right " + val[1].css + (val[1].max || val[1].min ? " bold" : "");
			var css2 = "right " + val[2].css + (val[2].max || val[2].min ? " bold" : "");
			var css3 = "right " + val[3].css + (val[3].max || val[3].min ? " bold" : "");
			var css4 = "right " + val[4].css + (val[4].max || val[4].min ? " bold" : "");
			var css5 = "right " + val[5].css + (val[5].max || val[5].min ? " bold" : "");
			var css6 = "right " + val[6].css + (val[6].max || val[6].min ? " bold" : "");
			var showChart = day.sum <= 0.0 ? 'style="display: none"' : '';

			var tr = '\
                        <tr>\
							<td class="left tipsy" original-title="' + day.fulldate + '">' + day.date + '</td>\
							<td class="' + css0 + '">' + val[0].value + '</td>\
							<td class="' + css1 + '">' + val[1].value + '</td>\
							<td class="' + css2 + '">' + val[2].value + '</td>\
							<td class="' + css3 + '">' + val[3].value + '</td>\
							<td class="' + css4 + '">' + val[4].value + '</td>\
							<td class="' + css5 + '">' + val[5].value + '</td>\
							<td class="' + css6 + '">' + val[6].value + '</td>\
							<td>\
								<a ' + showChart + 'id="a_chart_' + dayId + '" href="#" onclick="return hs_sandbox.makePieChart(tradingDays[' + i + '].values);">\
										<img src="images/riskMonitor/chartTypes/piechart16.png" alt="Chart">\
								</a>\
							</td>\
                        </tr>';
			trs += tr;
		}

		jQuery("#tblSandbox tbody tr:visible").remove();
		jQuery("#tblSandbox tbody .trNoData").css("display", trs === "" ? "table-row" : "none");

		jQuery("#tblSandbox tbody").append(trs);
		hs_sandbox.setOddEvenTableRows();
		hs_sandbox.setPaginationButtons();
		hs_sandbox.setEntriesPerPage(tradingDays.length);
	},
	uploadCheckSuffix: function() {
		var file = $("#upfile").val();
		if (file.indexOf(".csv") === -1) {
			alert("Only CSV files are accepted");
			$("#upfile").val("");
			return false;
		}
		return true;
	},
	uploadCheckBeforeSubmit: function() {
		var file = $("#upfile").val();
		if (file === "") {
			alert("Please select a CSV file");
			return false;
		}
		return hs_sandbox.uploadCheckSuffix();
	},
	uploadCsv: function() {
		var filename = $("#upfile").val();

//                http://stackoverflow.com/questions/6974684/how-to-send-formdata-objects-with-ajax-requests-in-jquery
		$.ajax({
			url: "/service/tradeFinder/tradefnd/content/riskMgmt/histsim/jsp/HistsimVarUpload.jsp",
			type: "POST",
			data: {
				file: filename,
				data: $("#upfile")[0].files[0]
			},
			processData: false,
			contentType: false,
			success: function(data) {
				alert(data);
			},
			error: function(xhr, text, error) {
				consoleLog(xhr);
				consoleLog(text);
				consoleLog(error);
			}
		});
	}
};