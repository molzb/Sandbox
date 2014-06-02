package com.db.tradefinder.taglib;

import com.db.tradefinder.web.controller.upload.FileUpload2014Controller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class FileUpload2014Tag extends TagSupport implements Tag {

	private static final Logger logger = LoggerFactory.getLogger(FileUpload2014Tag.class);
	/**
	 * required, validation on client side
	 */
	private String maxsize;
	private String suffices;
	private String uploadCmd = "";
	/**
	 * optional, validation on server side
	 */
	private String label = "+ Add files...";
	private String multiple;
	private String javaCallback = "";
	private String javascriptCallback = "";
	private String filenamePrefix = "";
	private String preview = "false";
	private String showPermissionsInSelectTag = "false";
	private String showUploadCmdsInSelectTag = "false";

	/**
	 * Used for testing the attribute 'javaCallback' in Example 4 of the Examples page 
	 * Link /.../TradeFinderServlet?menu_selection=fileuploadsample3)
	 * @return String with dummy output
	 */
	public String hello() {
		System.out.println("helloInMethod");
		return "hello returned";
	}

	@Override
	public int doStartTag() throws JspException {
		JspWriter out = pageContext.getOut();
		ServletContext servletContext = pageContext.getServletContext();
		String contextPath = servletContext.getContextPath();
		if (!contextPath.endsWith("/")) 
			contextPath += "/";
		String pathToFileUploadJsp = servletContext.getRealPath("jsp/util/FileUpload2014Tag.jspf");
		FileInputStream fileUploadForm = null;

		try {
			fileUploadForm = new FileInputStream(new File(pathToFileUploadJsp));
			byte[] buf = new byte[fileUploadForm.available()];
			fileUploadForm.read(buf);
			String fuFormContent = new String(buf);

			fuFormContent = fuFormContent.replace("{contextPath}", contextPath);
			fuFormContent = fuFormContent.replace("{maxsize}", maxsize);
			fuFormContent = fuFormContent.replace("{suffices}", suffices);
			fuFormContent = fuFormContent.replace("{uploadCmd}", uploadCmd);

			fuFormContent = fuFormContent.replace("{label}", label);
			if (multiple == null || multiple.equals("") || multiple.equals("false")) {
				fuFormContent = fuFormContent.replace("multiple", "");
			}
			fuFormContent = fuFormContent.replace("{filenamePrefix}", filenamePrefix);
			fuFormContent = fuFormContent.replace("{javaCallback}", javaCallback);
			fuFormContent = fuFormContent.replace("{javascriptCallback}", javascriptCallback);
			fuFormContent = fuFormContent.replace("{preview}", preview);
			fuFormContent = fuFormContent.replace("{previewDisplay}", preview.equals("false") ? "hidden" : "visible");
			fuFormContent = fuFormContent.replace("{permissionSelectTag}", 
					showPermissionsInSelectTag.equals("true") ? getPermissionsSelectTag() : "");
			
			// Multiple Upload commands, e.g. 'histsim,tso'? If so, then render select tag
			logger.info("showUploadCmds=" + showUploadCmdsInSelectTag);
			String selectTag = getUploadCmdsSelectTag(showUploadCmdsInSelectTag);
			logger.info("selectTag=" + selectTag);
			fuFormContent = fuFormContent.replace("{uploadCmdsSelectTag}", selectTag == null ? "" : selectTag);
			
			String uploadDirAndPerm = new FileUpload2014Controller().getUploadDirAndPermission(uploadCmd);
			if (uploadDirAndPerm != null && uploadDirAndPerm.contains(";")) {
				String uploadDir = uploadDirAndPerm.split(";")[0];
				boolean canWrite = false;
				File writeTestFile = new File(uploadDir + "/x.txt");
				try {
					File path = new File(uploadDir);
					if (!path.exists()) {
						path.mkdirs();
					}
					canWrite = writeTestFile.createNewFile();
					writeTestFile.delete();
				} catch (IOException ioe) {
					logger.error(ioe.getMessage(), ioe);
					logger.error("Target directory writable and existant? Error message:" + ioe.getMessage());
				}
				System.out.println(writeTestFile.getAbsolutePath() + " canWrite=" + canWrite);
				fuFormContent = fuFormContent.replace("{error}", canWrite ? "" : "Technical error");
				fuFormContent = fuFormContent.replace("{errorDisplay}", canWrite ? "none" : "inline-block");
				String relativePath = uploadDir.substring(uploadDir.indexOf("file_upload/") + "file_upload/".length());
				if (!relativePath.endsWith("/"))
					relativePath += "/";
				fuFormContent = fuFormContent.replace("{relativePath}", relativePath);
			} else {
				fuFormContent = fuFormContent.replace("{errorDisplay}", "none");
			}

			//TODO Request-Parameter sollten per POST weitergeleitet werden, scheitert aber derzeit
			//Daher Nutzung von Session-Attributen als Workaround
			pageContext.getSession().setAttribute("maxsize", maxsize);
			pageContext.getSession().setAttribute("suffices", suffices);
			pageContext.getSession().setAttribute("uploadCmd", uploadCmd);
			
			pageContext.getSession().setAttribute("multiple", multiple);
			pageContext.getSession().setAttribute("filenamePrefix", filenamePrefix);
			pageContext.getSession().setAttribute("javaCallback", javaCallback);
			pageContext.getSession().setAttribute("javascriptCallback", javascriptCallback);
			pageContext.getSession().setAttribute("preview", preview);
			pageContext.getSession().setAttribute("previewDisplay", preview.equals("false") ? "hidden" : "visible");
			out.println(fuFormContent);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
		} finally {
			if (fileUploadForm != null) {
				try {
					fileUploadForm.close();
				} catch (IOException ioe) {
					logger.info(ioe.getMessage());
				}
			}
		}
		return SKIP_BODY;
	}
	
	/**
	 * Renders HTML select tag containing permissions (label) and upload commands (value)
	 * @return [select][option...][/select]
	 */
	private String getPermissionsSelectTag() {
		StringBuilder sb = new StringBuilder();
		String permittedUploadDirs = new FileUpload2014Controller().getPermissionToUploadCmd();
		logger.info("permittedUploadDirs=" + permittedUploadDirs);
		if (!permittedUploadDirs.isEmpty()) {
			sb.append("\n\t<select id=\"selectPermission\">\n");
			String[] permCmds = permittedUploadDirs.split(";");
			for (int i = 0; i < permCmds.length; i += 3) {
				String cmdAndRelativePath = permCmds[i+1] + ";" + permCmds[i+2];
				sb.append("\t\t<option value=\"" + cmdAndRelativePath + "\">" + permCmds[i] + "</option>\n");
			}
			sb.append("\t</select>\n");
		}
		return sb.toString();
	}
	
	/**
	 * If the attribute 'uploadCmd' consists of several upload commands (e.g. histsim, tso, ld),
	 * then generate a HTML select tag to choose from the commands
	 * @param uploadCmds comma separated list, e.g. "histsim,tso,ld"
	 * @return HTML select tag, that will be inserted into the JSP template 'FileUpload2014Tag.jspf'
	 */
	private String getUploadCmdsSelectTag(String uploadCmds) {
		if (!uploadCmds.contains(",")) {
			return null;
		}
		if (uploadCmds.endsWith(",")) {
			logger.error("The attribute 'showUploadCmdsInSelectTag' ends with comma (','). This is not allowed");
			return null;
		}
		String[] uploadCmdsArray = uploadCmds.split(",");
		StringBuilder sb = new StringBuilder();
		sb.append("\n\t<select id=\"selectUploadCmd\">\n");
		for (String cmd : uploadCmdsArray) {
			sb.append("\t\t<option value=\"" +  cmd + "\">" + cmd + "</option>\n");
		}
		sb.append("\t</select>\n");
		return sb.toString();
	}

	/**
	 * Maximum size of the uploaded file in KB
	 *
	 * @param maxsize 1000 (for 1 MB)
	 */
	public void setMaxsize(String maxsize) {
		this.maxsize = maxsize;
	}

	/**
	 * Set the allowed suffices of the uploaded files
	 *
	 * @param suffices e.g. "csv, txt"
	 */
	public void setSuffices(String suffices) {
		this.suffices = suffices;
	}

	/**
	 * Cmd for the upload. Required for reading the file upload path and the required permission
	 * The parameter will be looked up in the DB table TF_SCREENER (field UPLOAD_CMD).
	 * @param uploadCmd Required for getting upload path and permission (see TF_SCREENER.UPLOAD_CMD)
	 */
	public void setUploadCmd(String uploadCmd) {
		this.uploadCmd = uploadCmd;
	}
	
	/**
	 * Multiple file upload, requires HTML 5 (i.e. [DOCTYPE !html] in the HTML template)
	 * @param trueOrFalse 
	 */
	public void setMultiple(String trueOrFalse) {
		this.multiple = trueOrFalse;
	}

	/**
	 * Multiple file upload, requires HTML 5 (i.e. [DOCTYPE !html] in the HTML template)
	 * @param label Value (text) of the button
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	public void setJavaCallback(String javaCallback) {
		this.javaCallback = javaCallback;
	}
	
	public void setJavascriptCallback(String javascriptCallback) {
		this.javascriptCallback = javascriptCallback;
	}

	public void setFilenamePrefix(String filenamePrefix) {
		this.filenamePrefix = filenamePrefix;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public void setShowPermissionsInSelectTag(String showPermissionsInSelectTag) {
		this.showPermissionsInSelectTag = showPermissionsInSelectTag;
	}

	public void setShowUploadCmdsInSelectTag(String showUploadCmdsInSelectTag) {
		this.showUploadCmdsInSelectTag = showUploadCmdsInSelectTag;
	}
}
