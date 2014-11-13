/* 
    Created on : 10.11.2014, 09:02:32
    Author     : Bernhard Molz
*/
"use strict";
var dragX = 0, dragY = 0, nowT = 0;
var dragDeltaX = 0, dragDeltaY = 0, deltaT = 0;
var deltaX = 0, deltaY = 0;
var pid = 0, timeId = 0;

var cntFinishs = 5;
var cntRemainFinishs = 5;
var cntStrokes = 0;
var cntTime = 90.0;
var score = 0;
var hiscore = 0;

var hiscores = [{name: "bm1", score: 5000},{name: "bm2", score: 4000},{name: "bm3", score: 3000},
	{name: "bm4", score: 2000},{name: "bm5", score: 1000}];
var CNT_HISCORES = 5; 

$(document).ready(function() {
	blinkArrows();

	$("canvas").mousedown(function(e) {
		dragX = e.pageX;
		dragY = e.pageY;
		nowT = window.performance.now();

		if (timeId === 0) {
			updateTime();
		}
	});
	$("canvas").mouseup(function(e) {
		dragDeltaX = e.pageX - dragX;
		dragDeltaY = e.pageY - dragY;
		deltaT = parseInt(window.performance.now() - nowT);
		deltaX = dragDeltaX / deltaT;
		deltaY = dragDeltaY / deltaT;
		$("#strokes").text(++cntStrokes);
		showDebug(deltaX, deltaY, deltaT);

		rotate(deltaX, deltaY);
	});
});

function updateTime() {
	var timeInterval = 100;
	var time = $("#time");
	timeId = window.setInterval(function() {
		cntTime -= 0.1;
		var strTime = Math.round(cntTime * 10) / 10;

		if (cntTime < 10.0 && !time.hasClass("timeWarn")) {
			time.addClass("timeWarn");
			playDingDingDing();
		}
		if (cntTime < 0.1) {
			window.clearInterval(pid);
			$("#outoftime").show(500);
			window.clearInterval(timeId);
		}

		time.text(strTime + " s");
	}, timeInterval);
}

function playDingDingDing() {
	var cntDing = 2;
	var ding = $("#ding")[0];
	ding.play();
	var dingdingdingId = window.setInterval(function() {
		console.log("ding");
		ding.play();
		cntDing--;
		if (cntDing === 0)
			window.clearInterval(dingdingdingId);
	}, 800);
}

function blinkArrows() {
	var blink = 0;
	var blinkId = window.setInterval(function() {
		var vis = $("#arrow1,#arrow2").css("visibility");
		$("#arrow1,#arrow2").css("visibility", vis === "hidden" ? "visible" : "hidden");
		if (blink++ >= 3) {
			window.clearInterval(blinkId);
			window.setTimeout(function() { $("#explanation").hide(); }, 1000);
		}
	}, 500);
}

function showDebug(deltaX, deltaY, deltaT) {
	$("#debug #dx").text(Math.round(deltaX * 100) / 100);
	$("#debug #dy").text(Math.round(deltaY * 100) / 100);
	$("#debug #dt").text(deltaT + " ms");
}

function draw() {
	var c = document.getElementById("myCanvas");
	var ctx = c.getContext("2d");

	var colors = [
		['blue', 'yellow'],
		['white', 'pink'],
		['green', 'red'],
		['white', 'black'],
		['green', 'blue'],
		['red', 'green', 'blue']
	];

	for (var i = 0; i < 12; i++) {
		for (var cIdx = 0, p = 0; p < 120; p += 20, cIdx++) {
			var cols = colors[cIdx];
			for (var r = p; r < p + 20; r++) {
				ctx.beginPath();
				ctx.arc(125, 125, r, (i - 1) * Math.PI / 6.0, i * Math.PI / 6.0);
				ctx.strokeStyle = cols[i % cols.length];
				ctx.stroke();
			}
		}
	}
}

function rotate(deltaX, deltaY) {
	var animSteps = 100;
	var deg = 0;
	var c = document.getElementById("myCanvas");
	if (pid > 0)
		window.clearInterval(pid);
	var x = parseFloat($(c).css("left")), y = parseFloat($(c).css("top"));
	pid = window.setInterval(function() {
		x += deltaX * 5;
		y += deltaY * 5;
		c.style.left = Math.round(x) + "px", c.style.top = Math.round(y) + "px";
		c.style.transform = 'rotate(' + deg + 'deg)';
		deg = (deltaX > 0) ? deg + 4 : deg -4;

		checkIfLost();
		readDistance();

		if (animSteps-- === 0) {
			window.clearInterval(pid);
		}
	}, 10);
}

function checkIfLost() {
	var canvas = $("#myCanvas");
	var width = parseInt(canvas.css("width")), height = parseInt(canvas.css("height"));
	var left  = parseInt(canvas.css("left")),  top = parseInt(canvas.css("top"));
	if (left < -(width/2) || top < -(height/2)) {
		if (pid > 0)
			window.clearInterval(pid);
		$("#lost").show();
		window.setTimeout(function() {
			$("#lost").hide();
			resetPosition();
		}, 2000);
	}
}

function resetPosition() {
	var body = $("body"), finish = $("#finish");
	var width = parseInt(body.css("width")), height = parseInt(body.css("height"));
	var maxX = width - parseInt(finish.css("width"));
	var maxY = height - parseInt(finish.css("height"));
	$("#myCanvas").css("left", maxX / 2 + "px").css("top", maxY / 2 + "px");
}

function readDistance() {
	var cx = parseInt( $("#myCanvas").css("left") ), cy = parseInt( $("#myCanvas").css("top") );
	var fx = parseInt( $("#finish").css("left") ), fy = parseInt( $("#finish").css("top") );
	var distX = cx - fx, distY = cy - fy;
	var distXY = parseInt(Math.sqrt(distX * distX + distY * distY));
	$("#debug #dDist").text( Math.round(distXY * 100) / 100 );
	if (distXY < 256) {
		changeBgFinish(distXY);
		if (distXY < 50) {
			if (cntRemainFinishs-- <= 0) {
				$("#done").show();
				window.clearInterval(timeId);
				score += parseInt(cntTime * 100);
				$("#score").text(score);
				if (score > hiscore) {
					hiscore = score;
					$("#hiscore").text(hiscore);
				}
			} else {
				moveFinish();
				score += 1000;
				$("#score").text(score);
			}
		}
	}
}

function changeBgFinish(distXY) {
	var bg = $("#finish").css("background-color"); // -> rgb(1,23,123)
	var rgbStr = bg.substring(4, bg.length-1);
	var rgb = rgbStr.split(",");
	var r = distXY, g = 255 - distXY, b = rgb[2];
	$("#finish").css("background-color", "rgb(" + r + "," + g + ", " + b + ")");
}

function moveFinish() {
	var body = $("body"), finish = $("#finish");
	var width = parseInt(body.css("width")), height = parseInt(body.css("height"));
	var maxX = width  - parseInt(finish.css("width"));
	var maxY = height - parseInt(finish.css("height"));
	var newX = parseInt(Math.random() * maxX), newY = parseInt(Math.random() * maxY);
	finish.css("left", newX + "px").css("top", newY + "px");
}

function showHiscores() {
	$(".splash.hiscores").show();
	var tbl = $(".hiscores table");
	for (var i = 0; i < CNT_HISCORES; i++) {
		tbl.find("tr:eq(" + (i+1) + ") td.score").text(hiscores[i].score);
		tbl.find("tr:eq(" + (i+1) + ") td.name").text(hiscores[i].name);
	}
}

function enterHiscore(myScore) {
	if (myScore === undefined) {
		console.warn("enterHiscore called without argument");
		return;
	}
	var tbl = $(".hiscores table");
	if (myScore > hiscores[CNT_HISCORES-1].score) {
		var pos = 0;
		for (pos = 0; pos < CNT_HISCORES; pos++) {
			if (myScore > hiscores[pos].score) {
				break;
			}
		}
		hiscores.splice(pos, 0, {name: "___", score: myScore});
		hiscores.splice(CNT_HISCORES,1);	// remove last element
		tbl.find("tr:eq(" + (pos+1) + ") td.score").text(myScore);
		tbl.find("tr:eq(" + (pos+1) + ") td.name").text("___");
	}
}