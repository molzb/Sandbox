/* 
 * Functions for StreamingLogServlet
 * @Author Bernhard
 */
$(document).ready(function() {
	$('#tabCatalina a, #tabTomcatBase a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});
	$("#filter .error, #filter .warn, #filter .info, #filter .text, #btnMark, #btnMarkRegex").
			data("toggle", "tooltip").
			data("placement", "top").
			tooltip();
});

var pollId = 0;

function updateLog() {
	$.ajax({url: "StreamingLogServlet?lines=" + $("#txtLines").val()}).success(function(html) {
		$(".log").html(html);
		pollLog();
		$("#btnUpdateLog").attr("disabled", true);
		$("#btnPauseLog").attr("disabled", false);
	});
}

function pollLog() {
	var interval = $("#txtPollInterval").val() * 1000;
	pollId = window.setInterval(function() {
		$.ajax({url: "StreamingLogServlet?bytesDiff=true", cache: false}).success(function(html) {
			$(".log").append(html);
			var tbl = $(".log")[0];
			tbl.scrollTop = tbl.scrollHeight;
		});
	}, interval);
}

function pauseLog() {
	if (pollId > 0)
		window.clearInterval(pollId);
	$("#btnUpdateLog").attr("disabled", false);
}

function clearLog() {
	$(".log").empty();
}

function mark() {
	if (markValidate()) {
		var term = $("#txtTerm").val();
		var markCount = 0;
		$(".log span").each(function() {
			$(this).removeClass("highlight");
			if ($(this).text().indexOf(term) > -1) {
				$(this).addClass("highlight");
				markCount++;
			}
		});
		$("#spanMark .count").text(markCount === 0 ? "Not found" : "Found " + markCount + " times");
	}
}
function markRegex() {
	if (markValidate()) {
		var term = $("#txtTerm").val();
		var markCount = 0;
		var regex = new RegExp(term);
		$(".log span").each(function() {
			$(this).removeClass("highlight");
			if ($(this).text().search(regex) > -1) {
				$(this).addClass("highlight");
				markCount++;
			}
		});
		$("#spanMark .count").text(markCount === 0 ? "Not found" : "Found " + markCount + " times");
	}
}
function markValidate() {
	var term = $("#txtTerm").val();
	if (term === "") {
		alert("Please enter some text");
		return false;
	}
	return true;
}

function filter(elem) {
	if ($(elem).hasClass("error")) {
		var cnt = filterText("ERROR ");
		$("#filter .count").text("Showing " + cnt + " lines");
	} else if ($(elem).hasClass("warn")) {
		filterText("WARN ");
		$("#filter .count").text("Showing " + cnt + " lines");
	} else if ($(elem).hasClass("info")) {
		filterText("INFO ");
		$("#filter .count").text("Showing " + cnt + " lines");
	} else if ($(elem).hasClass("text")) {
		filterText($("#txtFilter").val());
		$("#filter .count").text("Showing " + cnt + " lines");
	}
}

function filterText(text) {
	var sum = 0;
	jQuery(".log span").each(function() {
		if ($(this).text().indexOf(text) > -1) {
			$(this).show();
			sum++;
		} else {
			$(this).hide();
		}
	});
	return sum;
}
