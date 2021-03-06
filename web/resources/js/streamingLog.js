/* 
 * Functions for StreamingLog.disp
 * @Author Bernhard
 */
"use strict";

$(document).ready(function() {
	$('#tabCatalina a, #tabTomcatBase a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});
	// Tooltip doesn't work with button groups. TODO
	//	$("#tdFilter .error, #tdFilter .warn, #tdFilter .info, #tdFilter .text").	
	$(".btnMark, .btnMarkRegex").
			data("toggle", "tooltip").
			data("placement", "top").
			tooltip();

	// let's start with line 1 (not with 'lastLine' from last session)
	$.ajax({url: "StreamingLog.disp?clear=true"});
});

var pollId = 0;
var logInfoId = 0;

function updateLog() {
	$.ajax({url: "StreamingLog.disp?lines=" + $(".txtLines").val()}).success(function(html) {
		$(".log").html(html);
		pollLog();
		$(".btnUpdateLog").attr("disabled", true);
		$(".btnPauseLog").attr("disabled", false);
	});
}

function updateLogInfo() {
	$.ajax({url: "StreamingLogInfo.disp", dataType: "json", cache: false}).success(function(json) {
		var txt = "Len: " + json.len + ", Diff: " + json.diff + ", " + json.time;
		console.log("Text=" + txt);
		$(".logInfo").text(txt);
	}).fail(function(xhr, textStatus) {
		console.log(textStatus);
	});
}

function pollLog() {
	var interval = $(".txtPollInterval").val() * 1000;
	pollId = window.setInterval(function() {
		$.ajax({url: "StreamingLog.disp?bytesDiff=true", cache: false}).success(function(html) {
			$(".log").append(html);
			var tbl = $(".log")[0];
			tbl.scrollTop = tbl.scrollHeight;
		});
	}, interval);
	logInfoId = window.setInterval(function() {
		updateLogInfo();
	}, interval * 5);
	$(".logInfo").removeClass("error");
}

function pauseLog() {
	if (pollId > 0)
		window.clearInterval(pollId);
	if (logInfoId > 0)
		window.clearInterval(logInfoId);
	$(".btnPauseLog").attr("disabled", true);
	$(".btnUpdateLog").attr("disabled", false);
	$(".logInfo").addClass("error").text("Paused ...");
}

function clearLog() {
	$.ajax({url: "StreamingLog.disp?clearParameter=true"});
	$(".log").empty();
}

function mark() {
	if (markValidate()) {
		var term = $(".txtTerm").val();
		var markCount = 0;
		$(".log span").each(function() {
			$(this).removeClass("highlight");
			if ($(this).text().indexOf(term) > -1) {
				$(this).addClass("highlight");
				markCount++;
			}
		});
		$(".spanMark .count").text(markCount === 0 ? "Not found" : "Found " + markCount + " times");
	}
}
function markRegex() {
	if (markValidate()) {
		var term = $(".txtTerm").val();
		var markCount = 0;
		var regex = new RegExp(term);
		$(".log span").each(function() {
			$(this).removeClass("highlight");
			if ($(this).text().search(regex) > -1) {
				$(this).addClass("highlight");
				markCount++;
			}
		});
		$(".spanMark .count").text(markCount === 0 ? "Not found" : "Found " + markCount + " times");
	}
}
function markValidate() {
	var term = $(".txtTerm").val();
	if (term === "") {
		alert("Please enter some text");
		return false;
	}
	return true;
}

function filter(elem) {
	var cnt = 0;
	if ($(elem).hasClass("all")) {
		$(".log > span").show();
		$(".tdFilter .count").text("Show all");
	} else if ($(elem).hasClass("error")) {
		cnt = filterText("ERROR ");
		$(".tdFilter .count").text("Showing " + cnt + " lines");
	} else if ($(elem).hasClass("warn")) {
		cnt = filterText("WARN ");
		$(".tdFilter .count").text("Showing " + cnt + " lines");
	} else if ($(elem).hasClass("info")) {
		cnt = filterText("INFO ");
		$(".tdFilter .count").text("Showing " + cnt + " lines");
	} else if ($(elem).hasClass("text")) {
		cnt = filterText($(".txtFilter").val());
		$(".tdFilter .count").text("Showing " + cnt + " lines");
	}
}

function filterText(text) {
	var sum = 0;
	jQuery(".log > span").each(function() {
		if ($(this).text().indexOf(text) > -1) {
			$(this).show();
			sum++;
		} else {
			$(this).hide();
		}
	});
	return sum;
}