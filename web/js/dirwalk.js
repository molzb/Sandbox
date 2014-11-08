$(document).ready(function() {
	$("li.dir").addClass("expanded");

	$("li.file, li.dir").click(function() {
		$("#filename").text($(this).mytext());
		$("#type").text($(this).hasClass("file") ? "File" : "Directory");
		$("#location").text(dirwalk.getPath(this));


		if ($(this).hasClass("dir")) {
			dirwalk.showDir($("#filename").text());
		} else if ($(this).hasClass("file")) {
			dirwalk.showFile($("#filename").text());
		}
		return false;	// wichtig, sonst Event-Bubbling
	});

//	$("#tree").jstree();
});

var dirwalk = {
	getPath: function(htmlElem) {
		var path = [], elem = $(htmlElem);
//	path.push($(htmlElem).mytext());
		while (true) {
			if (elem.parent().parent()[0].nodeName === "LI") {
				elem = elem.parent().parent();
				path.push(elem.mytext());
			}
			else
				break;
		}
		return path.reverse().join("/");
	},
	jsonStr: "",
	toggleDir: function(jqElem) {
		if (jqElem.hasClass("dir")) {
			jqElem.children().toggle();
			if (jqElem.hasClass("expanded")) {
				jqElem.removeClass("expanded").addClass("closed");
			} else {
				jqElem.removeClass("closed").addClass("expanded");
			}
		}
	},
	searchFile: function(text) {
		console.log("TODO " + text);
	},
	showDir: function(filename) {
		console.log("showDir " + filename);
		var json;
		$.ajax({url: "filesInDir.action", data: {"filename":filename}, dataType: json})
				.done(function(data, textStatus, xhr) {
					console.log(data);
					console.log(textStatus);
					console.log(xhr);
					dirwalk.jsonStr = JSON.parse(xhr.responseText);
				})
				.fail(function() {
					alert("error");
				})
				.always(function() {
					alert("complete");
				});
	},
	showFile: function(filename) {
		console.log("showFile " + filename);
		var json = $.ajax({url: "fileDetails.action", data: {"filename": filename}, dataType: json})
				.done(function(data, textStatus, xhr) {
					console.log(data);
					console.log(textStatus);
					console.log(xhr);
					dirwalk.jsonStr = JSON.parse(xhr.responseText);
				})
				.fail(function() {
					alert("showFile error");
				})
				.always(function() {
					alert("showFile complete");
				});
	}
};

$.fn.mytext = function() {
	var str = '';

	this.contents().each(function() {
		if (this.nodeType === 3) {
			str += this.textContent || this.innerText || '';
		}
	});

	return str.trim();
};

