jQuery(document).ready(function() {
	jQuery("#batchUserdialog").dialog({
		width: 700,
		height: 500,
		autoOpen: false
	});
});

var batchUser = {
	users: new Array(),
	csvFilename: "35085-1_20140415_leaver.csv",	// just for debugging
	unzip: function() {
		var filenamePrefix = jQuery("input#filenamePrefix").val();
		jQuery.ajax({url: "batchUser/unzipUploadedCsv.disp?filename=" + filenamePrefix + upload.uploadedFile.name, 
			dataType: "text", 
			cache: false,
			success: function(data) {
				batchUser.csvFilename = data;
				batchUser.previewCsv(data);
			}
		});
	},
	
	previewCsv: function() {
		jQuery.ajax({
			dataType: "json",
			url: "batchUser/batchUserJson.disp?csvFilename=" + batchUser.csvFilename,
			cache: false,
			success: function(data) {
				batchUser.users = data;
				batchUser.users.sort(function(o1, o2) {
					if (o1.account === o2.account)
						return 0;
					return (o1.account > o2.account) ? 1 : -1;
				});
				batchUser.renderTable(batchUser.users);
			}
		});
	},
	renderTable: function(users) {
		//account, owner, action, reason, requestNo, creationDate
		var trs = "";
		for (i = 0; i < users.length; i++) {
			user = users[i];
			var tr = "<tr>\\n\
							<td title='Owner " + user.owner + "'>" + user.account + "</td>\\n\
							<td>" + user.action + "</td>\\n\
							<td>" + user.reason + "</td>\\n\
							<td>" + user.requestNo + "</td>\\n\
							<td>" + user.creationDate + "</td>\\n\
						</tr>";
			trs += tr;
		}
		jQuery("#numberOfUsers").text(users.length);
		jQuery("#tblBatchUserPreview tbody").html(trs);
		jQuery("#tblBatchUserPreview tbody tr:even").addClass("even");
		jQuery("#tblBatchUserPreview tbody tr:odd").addClass("odd");
		window.setTimeout(function() {
			batchUser.adjustStaticTableHeaders();
		}, 250);

		jQuery("#batchUserdialog").dialog("open");
	},
	adjustStaticTableHeaders: function() {
		var cols = jQuery("#tblBatchUserPreview tbody tr:eq(0) td").length;
		for (i = 0; i < cols; i++) {
			var tr = "#tblBatchUserPreview tbody tr:eq(0)";
			var width = jQuery(tr).find("td:eq(" + i + ")").css("width");
			jQuery("#tblBatchUserHeaders th:eq(" + i + ")").css("width", width);
		}
	},
	lockUsers: function() {
		jQuery.ajax({url: "batchUser/lockUsersInDB.disp", cache: false, dataType: "text",
			success: function(data) {
				if (data === "FAIL") {
					alert("Technical error. Please contact bernhard.molz@db.com");
				} else {
					alert("All users have been locked");
					jQuery("#batchUserdialog").dialog("close");
				}
			}
		});
	}
};
