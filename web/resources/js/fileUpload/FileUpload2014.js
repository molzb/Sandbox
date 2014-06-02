/**
 * FileUpload 2014
 */

/**
 * Workaround for console.log in IE
 * @param {string} s to display in console
 */
function consoleLog(s) {
	if ('console' in self && 'log' in console) {
		console.log(s);
	}
}
function helloGlobal() {
	var msg = "This is the javscriptCallback helloGlobal()";
	if (upload.uploadedFile !== null && upload.uploadFile !== "") {
		var f = upload.uploadedFile;
		msg += "\n\nThe file you have just uploaded is: name=" +
				f.name + ", size=" + f.size + ", type=" + f.type;
		msg += "\nCheck it by yourself: type 'upload.uploadedFile.name' in the JS console.";
	}
	alert(msg);
}

var upload = {
	uploadedFiles: "This array will be filled AFTER you have uploaded a file",
	uploadedFile: "",
	contextPath: jQuery("body").data("context-path"),
	highlight: function(str) {
		var code = jQuery(".tagSource pre");
		code.html( code.html().replace(str, "<span class='highlight'>" + str + "</span>") );
		return code;
	},
	/**
	 * Makes Ajax call to delete a file in the fileupload dir
	 * @param {string} filename Filename without suffix / prefix
	 * @param {jq} tr table row
	 * @returns {undefined}
	 */
	deleteFileFromServer: function(filename, tr) {
		var uploadCmd = tr.data("uploadcmd");	// data only with minor letters, so no upload_C_md
		var prefix = jQuery("#uploadDivWrapper input#filenamePrefix").val();
		var url = upload.contextPath + "fileupload/deleteFileFromServer.disp?filename=" + prefix + filename + "&uploadCmd=" + uploadCmd;
		jQuery.ajax({ url: url, cache: false }).done(function(data) {
			if (data === "CMD_NOT_FOUND" || data === "FILE_NOT_FOUND") {
				consoleLog("ERROR in deleteFileFromServer: " + data);
				jQuery("#fileupload").attr("disabled", "disabled").attr("title", "Disabled due to technical error");
				jQuery("#uploadDivWrapper .alert-message.error").show().html("Technical error. Please contact your administrator.");
			} else {
				consoleLog(prefix + filename + " deleted.");
				tr.remove();
			}
		});
	},
	cancelUpload: function(filename) {
		alert("Upload cancelled: " + filename);
		consoleLog("Upload cancelled: " + filename);
		jqXHR.abort();
	},
	showErrorButton: function(uploadError) {
		jQuery(".alert-message.error").remove();
		var btnError = new String(upload.buttonError).replace("{msg}", uploadError);
		jQuery(".fileinput-button").after(btnError);
		jQuery("#uploadDivWrapper .alert-message.error").show().fadeOut(4000);
	},
	buttonError:
		'<div style="display: inline; margin: 2px 5px;" class="alert-message block-message error">\
			<span>{msg}</span>\
		</div>',
	buttonSuccess:
		'<div style="display: inline; margin: 2px 5px;" class="alert-message block-message success">\
			<span>{msg}</span>\
		</div>',
	jqXHR: null,
	initValidation: function() {
			var isOldIE = navigator.userAgent.indexOf("MSIE 8") > -1 || navigator.userAgent.indexOf("MSIE 7") > -1;
		if ( isOldIE && document.compatMode === "BackCompat") {
			alert("The File Upload doesn't support Internet Explorer in Quirks Mode.\n" +
					"Please use <!DOCTYPE html> in your page to make it standard compliant.");
			return false;
		}
		if (jQuery.fn.jquery.indexOf("1.4") > -1) {
			alert("The File Upload doesn't work with this JQuery-Version: " + jQuery.fn.jquery);
			return false;
		}
		return true;
	},
	init: function() {
		consoleLog("init");
		if (!this.initValidation())
			return;
		
		var uploadCmd = jQuery("#uploadDivWrapper input#uploadCmd").val();
		jQuery.ajax({ url: upload.contextPath + "fileupload/uploadDirAndPermission.disp?uploadCmd=" + uploadCmd }).done(function(data) {
			if (data === "CMD_NOT_FOUND") {
				jQuery("#fileupload").attr("disabled", "disabled").attr("title", "Disabled due to technical error");
				upload.showErrorButton("Technical error. Please contact your administrator.");
			}
		});

		jQuery("#uploadDivWrapper #selectPermissionDiv").change(function() {
			var selectVal = jQuery(this).find("select").val();
			var uploadCmd = selectVal.split(";")[0],
					relativePath = selectVal.split(";")[1];
			jQuery("input#uploadCmd").val(uploadCmd);
			jQuery("input#relativePath").val(relativePath);
			var urli = upload.contextPath + "fileupload/setSessionAttribute.disp?name=uploadCmd&value=" + uploadCmd;
			jQuery.ajax({url: urli, cache: false});
		});
		jQuery("#uploadDivWrapper #selectUploadCmdsDiv select").change(function() {
			jQuery("input#uploadCmd").val(jQuery(this).val());
			var urli = upload.contextPath + "fileupload/setSessionAttribute.disp?name=uploadCmd&value=" + uploadCmd;
			jQuery.ajax({url: urli, cache: false});
		});

		this.jqXHR = jQuery("#fileupload").fileupload({
			dataType: "json",
			maxFileSize: jQuery(this).data("maxsize") << 10,
			formData: {
				"filename": jQuery("#uploadDivWrapper input#filename").val(),
				"filenamePrefix": jQuery("#uploadDivWrapper input#filenamePrefix").val(),
				"uploadCmd": jQuery("#uploadDivWrapper input#uploadCmd").val(),
				"javaCallback": jQuery("#uploadDivWrapper input#javaCallback").val(),
				"javascriptCallback": jQuery("#uploadDivWrapper input#javascriptCallback").val(),
				"label": jQuery("#uploadDivWrapper input#label").val(),
				"preview": jQuery("#uploadDivWrapper input#preview").val()
			},
			add: function(e, data) {
				consoleLog(data);
				consoleLog("add " + data.files.length + " files, loaded=" + data.loaded + ", total=" + data.total);
				consoleLog(data.files);
				upload.uploadedFiles = data.files;
				upload.uploadedFile = data.files[0];
				var uploadError = "";

				var filename = data.files[0].name;
				if (!upload.checkSuffix(filename)) {
					uploadError = "Not an accepted file type. Please choose: " + jQuery(this).data("suffices");
				}
				else if (data.originalFiles[0]['size'] > (jQuery(this).data("maxsize") << 10)) {
					uploadError = "Filesize is too big. Max " + jQuery(this).data("maxsize") + " KB";
				}
				if (uploadError !== "") {
					consoleLog("uploadError " + uploadError);
					upload.showErrorButton(uploadError);
				} else {	
					// Show 'Cancel Upload' button during the file upload. It will be hidden when the upload is done
					jQuery("#cancelUpload").show().off("click");
					jQuery("#cancelUpload").click(function() {
						upload.cancelUpload(upload.uploadedFile.name);
					});

					data.submit();
				}
			},
			success: function(result, textStatus, jqXHR) {
				consoleLog("success textStatus=" + textStatus);
				upload.afterDone(result);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				jQuery("#cancelUpload").hide();
				consoleLog("error: textStatus=" + textStatus + ", errorThrown=" + errorThrown);
//				consoleLog(jqXHR);
				if (errorThrown === "Not Found" && jqXHR.status === 404) {
					alert("Upload to server failed");
					consoleLog("The upload URL is probably wrong, HTTP status: 404.");
				}
				else if (errorThrown === "abort") {
					alert('File Upload has been canceled. TODO: delete DOM elements');
				}
			},
			progress: function(e, data) {
				var progress = parseInt(data.loaded / data.total * 100, 10);
				consoleLog("progress=" + progress + ", loaded=" + data.loaded + ", total=" + data.total);
				jQuery("#progress .progress-bar-success").css("width", progress + "100%");
			},
			progressall: function(e, data) {
				var progress = parseInt(data.loaded / data.total * 100, 10);
				consoleLog("progressAll=" + progress + ", loaded=" + data.loaded + ", total=" + data.total);
			}
		});
	},
	afterDone: function(result) {
		jQuery("#cancelUpload").hide();
		jQuery.ajax({url: upload.contextPath + "fileupload/virusMsg.disp"}).done(function(data) {
			if (data !== "OK") {
				upload.showErrorButton("Virus found. File has been deleted.");
			} else {	// everything's ok: show button (and preview)
				jQuery("#uploadDivWrapper .alert-message.error").hide();
//				var msg = upload.uploadedFile.name + " uploaded";
				var btnSuccess = new String(upload.buttonSuccess).replace("{msg}", result.msg);
				jQuery(".fileinput-button").after(btnSuccess);

				if (jQuery("#uploadDivWrapper input#preview").val() === "true") {
					upload.addPreviewEntry(upload.uploadedFiles);
				}
			}
		});

		// Javascript Callback
		var jsCallback = jQuery("#uploadDivWrapper input#javascriptCallback").val();
		upload.executeFunctionByName(jsCallback, window, null);
	},
	executeFunctionByName: function(functionName, context /*, args */) {
		if (functionName === null || functionName === "")
			return;
		var args = [].slice.call(arguments).splice(2);
		var namespaces = functionName.split(".");
		var func = namespaces.pop();
		consoleLog("func=" + func);
		for (var i = 0; i < namespaces.length; i++) {
			context = context[namespaces[i]];
		}
		return context[func].apply(this, args);
	},
	uploadCallback: function(filename) {
		if (filename === null || filename === "") {
			alert("Technical error");
			consoleLog("ERROR in uploadCallback: filename is [" + filename + "]");
			return false;
		}
		var hasPermission = new Boolean(jQuery("#hasPermission").val() === "true");
		var hasVirus = new Boolean(jQuery("#hasVirus").val() === "true");
		var virusMsg = jQuery("#virusMsg").val();
		if (!hasPermission) {
			alert("You don't have the permission");
			return false;
		}
		if (!hasVirus) {
			alert("The uploaded file contains a virus. Msg: " + virusMsg);
			return false;
		}
		return true;
	},
	jsCallback: function() {
		alert("You might want to set a javascript callback after your files have been uploaded");
	},
	checkSuffix: function(uploadfileSuffix) {
		var dataSuffices = jQuery("#fileupload").data("suffices");
		var suffices = dataSuffices.split("|");
		consoleLog("checkSuffix: uploadfileSuffix=" + uploadfileSuffix + ", suffices=" + suffices);

		var suffixFound = false;
		for (var i = 0; i < suffices.length; i++) {
			if (uploadfileSuffix.indexOf("." + suffices[i]) > -1) {
				suffixFound = true;
				break;
			}
		}
		return suffixFound;
	},
	previewTemplate:
		'	<tr class="template-upload" data-uploadcmd="{uploadCmd}">\n\
				<td>{previewTag}</td>\n\
				<td><p class="filename">{filename}</p></td>\n\
				<td><p class="size">{size}</p></td>\n\
				<td>\n\
					<span class="btn btn-danger delete" \n\
						onclick="upload.deleteFileFromServer(\'{filename}\', jQuery(this).parent().parent())">\n\
						<i class="glyphicon glyphicon-trash"></i>\n\
						<span>Delete</span>\n\
					</span>&nbsp;\n\
				</td>\n\
			</tr>',
	previewImgTag:
		'	<img src="{contextPath}protected/file_upload/{relativePath}{filenamePrefix}{filename}" alt="{filename}"\n\
					width="70px" height="50px"/></td>\n',
	previewTxtTag:
		'	<iframe width="200" height="50" frameborder="0" style="border: 1px solid #CCCCCC" \n\
				src="{contextPath}fileupload/excerptFromUploadedFile.disp?uploadCmd={uploadCmd}&filename={filenamePrefix}{filename}">\n\
				No IFrames\n\
			</iframe>',
	previewOtherTag:
		'	<span style="border: 1px solid #CCCCCC; padding: 5px">{txt}</span>',
	addPreviewEntry: function(files) {
		consoleLog("addPreviewEntry " + files.length);
		if (files === null || files.length === 0) {
			consoleLog("WARNING: addPreviewEntry called with NULL parameter");
			return;
		}

		var tbody = jQuery(".preview table tbody");
		var rows = "";
		for (var i = 0; i < files.length; i++) {
			var f = files[i];
			var tpl = new String(this.previewTemplate);
			var relativePath = jQuery("input#relativePath").val();
			var filenamePrefix = jQuery("input#filenamePrefix").val();
			var uploadCmd = jQuery("input#uploadCmd").val();

			var suffices = jQuery("#fileupload").data("suffices");
			var previewTag = "";
			if (suffices.indexOf("img") > -1 || suffices.indexOf("jpg") > -1 || suffices.indexOf("png") > -1) {
				previewTag = new String(this.previewImgTag);
				previewTag = previewTag.replace(/{filename}/g, f.name).
						replace("{contextPath}", upload.contextPath).
						replace("{filenamePrefix}", filenamePrefix).
						replace("{relativePath}", relativePath);
			} else if (suffices.indexOf("txt") > -1 || suffices.indexOf("csv") > -1) {
				previewTag = new String(this.previewTxtTag);
				previewTag = previewTag.replace("{filename}", f.name).
						replace("{contextPath}", upload.contextPath).
						replace("{filenamePrefix}", filenamePrefix).
						replace("{uploadCmd}", uploadCmd);
			} else {
				previewTag = new String(this.previewOtherTag);
				previewTag = previewTag.replace("{txt}", "No preview available for " + f.name);
			}

			var tr = tpl.replace(/{filename}/g, f.name).
					replace("{uploadCmd}", uploadCmd).
					replace("{previewTag}", previewTag).
					replace("{filenamePrefix}", filenamePrefix).
					replace("{relativePath}", relativePath).
					replace("{size}", Math.round(f.size >> 10) + " KB");
			rows += tr;
		}
		window.setTimeout(function() {
			tbody.append(rows);
			jQuery(".table tr:even td").addClass("even");
			jQuery(".table tr:odd td").addClass("odd");
		}, 2500);
	},
	toggleHtmlSource: function() {
		var head = '<head>\n' +
				'\t<script src="resources/js/fileUpload/FileUpload2014.js" type="text/javascript"></script>\n' +
				'\t<script src="resources/js/fileUpload/jquery.fileupload.all.js" type="text/javascript"></script>\n' +
				'\t<script type="text/javascript">jQuery(function() {	upload.init(); 	});</script>\n' +
				'\t<link rel="stylesheet" type="text/css" href="resources/css/FileUpload2014.css">\n' +
				'<\head>\n';

		var htmlSourceVisible = jQuery.trim(jQuery(".htmlSource pre").text()) === "";
		jQuery(".htmlSource pre").text(htmlSourceVisible ?
				head + jQuery("#uploadDivWrapper").html() : "");
		if (htmlSourceVisible) {
			jQuery(".htmlSource pre").removeClass("prettyprinted");
			prettyPrint();
		}
		jQuery(".htmlSource a").text(htmlSourceVisible ?
				"- Hide generated HTML" : "+ Show generated HTML");
	}
};