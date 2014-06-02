package com.db.tradefinder.web.controller.upload;

import com.db.tomcat.tfsecurity.TFDetailedPermission;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.db.tomcat.tfsecurity.TFSecurity;
import com.db.tradefinder.persistence.DBUtil;
import com.db.tradefinder.persistence.PMException;
import com.db.tradefinder.service.factory.ConnectionFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Controller for the File Upload tag [tf:upload]
 * @author Bernhard
 */
@Controller
@RequestMapping(value = "fileupload")
public class FileUpload2014Controller {

	private static final Logger logger = LoggerFactory.getLogger(FileUpload2014Controller.class);

	private static final String EXIT_OK = "OK", EXIT_FAIL = "FAIL";

	enum ERROR_CODES {
		VIRUS_DETECTED, MKDIR_FAILED, PERMISSION_FAILED, CMD_NOT_FOUND, FILE_NOT_FOUND, DB_DOWN, MOVE_FILE_FAILED
	};

	@Autowired
	ServletContext servletContext;

	private final boolean hasVirus = false;
	private String virusMsg = "";

	@RequestMapping(value = "/uploadFileToServer.disp", produces = "text/plain")
	@ResponseBody
	public String uploadFileToServer(HttpServletRequest request) {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			logger.warn("isMultipart = false");
			return "Do not access this page directly, this is the endpoint for the tf:upload-Tag";
		} else {
			HttpSession sess = request.getSession();
			String filenamePrefix = request.getParameter("filenamePrefix") == null
					? sess.getAttribute("filenamePrefix").toString()
					: request.getParameter("filenamePrefix");
			String cmd = request.getParameter("uploadCmd") == null
					? sess.getAttribute("uploadCmd").toString()
					: request.getParameter("uploadCmd");
			String javaCallback = request.getParameter("javaCallback") == null
					? sess.getAttribute("javaCallback").toString()
					: request.getParameter("javaCallback");
			String[] targetDirAndPermission = getUploadDirAndPermission(cmd).split(";");
			if (targetDirAndPermission == null || targetDirAndPermission.length == 1) {
				return jsonMsg(ERROR_CODES.CMD_NOT_FOUND.name());
			}
			logger.info("targetDirAndPermission=" + Arrays.toString(targetDirAndPermission) 
					+ ", filenamePrefix=" + filenamePrefix);
			String targetDir = targetDirAndPermission[0];
			String permission = targetDirAndPermission[1];
			if (isPermitted(permission).equals("false")) {
				return jsonMsg(ERROR_CODES.PERMISSION_FAILED.name());
			}
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			String filename = "";
			String filenames = "";
			try {
				List items = upload.parseRequest(request);
				Iterator<FileItem> iterator = items.iterator();
				List<File> uploadedFiles = new ArrayList<File>();
				while (iterator.hasNext()) {
					FileItem item = iterator.next();

					if (!item.isFormField()) {
						String itemName = item.getName();
						//IE8 returns a full pathname in item.getName(), e.g. C:\local\test.txt, so we have
						//to prevent filenames in the upload dir like protected/fileupload/C:\\local\\test.txt
						if (itemName.contains("\\") || itemName.contains("/")) {
							int indexOfSlash = itemName.indexOf('\\') > -1 ?
									itemName.lastIndexOf('\\') : itemName.lastIndexOf('/');
							itemName = itemName.substring(indexOfSlash + 1);
							logger.info("itemName before/after=" + item.getName() + "/" + itemName);
						}
						filename = filenamePrefix + itemName;
						if (servletContext == null) {
							servletContext = request.getSession().getServletContext();
						}
						File scanpath = new File(servletContext.getRealPath("/protected/file_upload/_virus_scan/"));
						if (!scanpath.exists()) {
							if (!scanpath.mkdirs()) {
								logger.error("Can't create path for virus scanner: " + scanpath + ". FAIL!");
								return jsonMsg(ERROR_CODES.MKDIR_FAILED.name());
							}
						}
						File scanpathFile = new File(scanpath + "/" + filename);
						logger.info("scanpathFile=" + scanpathFile.getAbsolutePath());
						item.write(scanpathFile);

						RunVirusScan virusScan = new RunVirusScan();
						boolean virusFound = virusScan.scan(filename) > 0;
						virusMsg = virusScan.getOutput();
						sess.setAttribute("virusFound", virusFound);
						sess.setAttribute("virusMsg", virusMsg);
						
						if (virusFound) {
							logger.warn("User " + TFSecurity.getTFUser().getUsername() + " tried to uploaded "
									+ "a virus infected file: " + filename);
							logger.warn("Message of virus scanner: " + virusMsg);
							scanpathFile.delete();
							return jsonMsg(ERROR_CODES.VIRUS_DETECTED.name());
						}
						File fupath = new File(targetDir);
						logger.info("fupath=" + fupath.getAbsolutePath());
						if (!fupath.exists()) {
							if (!fupath.mkdirs()) {
								logger.error("Can't create path for file upload: " + fupath + ". FAIL!");
								return jsonMsg(ERROR_CODES.VIRUS_DETECTED.name());
							}
						}
						if (virusScan.getExitValue() == 0) {
							File uploadedFile = new File(fupath + "/" + filename);
							boolean fileMoved = scanpathFile.renameTo(uploadedFile);
							if (!fileMoved) {
								logger.error("Cannot move the file to this destination: " + uploadedFile);
								logger.error("The target directory is probably not writable. Use chmod");
								return jsonMsg(ERROR_CODES.MOVE_FILE_FAILED.name());
							}
							
							uploadedFiles.add(uploadedFile);
							logger.info("+++ uploadedFiles=" + uploadedFiles.toString());
							filenames += filename;
							if (iterator.hasNext())
								filenames += ", ";
							javaCallback(javaCallback);
						} else {
							javaCallback(javaCallback);
							return jsonMsg(virusScan.getOutput());
						}
					}
				}
				sess.setAttribute("uploadedFiles", uploadedFiles);
				return jsonMsg(filenames + " uploaded.");
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			return jsonMsg(filename);
		}
	}
	
	private String jsonMsg(String msg) {
		return "{\"msg\": \"" + msg + "\"}";
	}
	
	@RequestMapping(value = "/setSessionAttribute.disp", produces = "text/plain")
	@ResponseBody
	public String setSessionAttribute(@RequestParam(required = true) String name, 
			@RequestParam(required = true) String value, HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute(name, value);
		return name + "=" + value;
	}
	
	public boolean getHasVirus() {
		return hasVirus;
	}

	public String getVirusMsg() {
		return virusMsg;
	}
	
	@RequestMapping(value = "/excerptFromUploadedFile.disp", produces = "text/html")
	@ResponseBody
	public String excerptFromUploadedFile(@RequestParam(required = true) String filename, 
			@RequestParam(required = true) String uploadCmd) {
		String[] uploadDirPermission = getUploadDirAndPermissionStrings(uploadCmd);
		if ("false".equals(isPermitted(uploadDirPermission[1]))) {
			return ERROR_CODES.PERMISSION_FAILED.name();
		}
		File file = new File(uploadDirPermission[0] + "/" + filename);
		if (!file.exists()) {
			logger.error(file.getAbsolutePath() + " doesn't exist");
			return ERROR_CODES.FILE_NOT_FOUND.name();
		}
		String excerpt = "";
		BufferedReader bis;
		try {
			bis = new BufferedReader(new FileReader(file));
			int linesCount = 0;
			String line;
			while ( (line = bis.readLine()) != null && linesCount++ < 3) {
				if (line.contains("<") || line.contains(">"))
					line = line.replace("<", "&lt;").replace(">", "&gt;");
				excerpt += line.length() > 20 ? line.substring(0, 20) + "...<br>" : line + "<br>";
			}
			bis.close();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}

		return excerpt;
	}
	
	@RequestMapping(value = "/deleteFileFromServer.disp", produces = "text/plain")
	@ResponseBody
	public String deleteFileFromServer(@RequestParam(required = true) String filename, 
			@RequestParam(required = true) String uploadCmd) {
		String uploadDirAndPermission = getUploadDirAndPermission(uploadCmd);
		if (!uploadDirAndPermission.contains(";")) {
			return uploadDirAndPermission;
		}
		String uploadDir = uploadDirAndPermission.split(";")[0];
		String permission = uploadDirAndPermission.split(";")[1];

		if ("false".equals(isPermitted(permission))) {
			return ERROR_CODES.PERMISSION_FAILED.name();
		}
		File file = new File(uploadDir + "/" + filename);
		if (!file.exists()) {
			logger.error(file.getAbsolutePath() + " doesn't exist");
			return ERROR_CODES.FILE_NOT_FOUND.name();
		}
		boolean deleted = file.delete();
		logger.info("delete " + file.getAbsolutePath() + "? " + (deleted ? EXIT_OK : EXIT_FAIL));
		return EXIT_OK;
	}
	
	public String[] getUploadDirAndPermissionStrings(String uploadCmd) {
		String uploadDirAndPermission = getUploadDirAndPermission(uploadCmd);
		if (!uploadDirAndPermission.contains(";")) {
			return new String[] { uploadDirAndPermission };
		}
		return uploadDirAndPermission.split(";");
	}
	
	@RequestMapping(value = "/virusMsg", produces = "text/plain")
	@ResponseBody
	public String getVirusMsg(HttpServletRequest request) {
		HttpSession session = request.getSession(); 
		boolean virusFound = session.getAttribute("virusFound") == null ? false : (Boolean)(session.getAttribute("virusFound"));
		return virusFound ? (String)request.getSession().getAttribute("virusMsg") : "OK";
	}

	@RequestMapping(value = "/permissionAndVirusScan.disp", produces = "text/plain")
	@ResponseBody
	public String doPermissionAndVirusScan(@RequestParam(required = true) String uploadCmd,
			@RequestParam(required = true) String filename) {
		String uploadDirAndPermission = getUploadDirAndPermission(uploadCmd);
		if (!uploadDirAndPermission.contains(";")) {
			return uploadDirAndPermission;
		}
		String permission = uploadDirAndPermission.split(";")[1];

		if ("false".equals(isPermitted(permission))) {
			return ERROR_CODES.PERMISSION_FAILED.name();
		}
		return doVirusScan(filename);
	}

	@RequestMapping(value = "/isPermitted.disp", produces = "text/plain")
	@ResponseBody
	public String isPermitted(@RequestParam(required = true) String permission) {
		if (permission == null) {
			logger.warn("checkPermission: permission = null!");
		}
		return permission != null && TFSecurity.getTFUser().isPermitted(permission) ? "true" : "false";
	}

	@RequestMapping(value = "/virusScan.disp")
	@ResponseBody
	public String doVirusScan(@RequestParam(required = true) String filename) {
		File scanpath = new File(servletContext.getRealPath("/protected/file_upload/_virus_scan/"));
		if (!scanpath.exists()) {
			if (!scanpath.mkdirs()) {
				logger.error("Can't create path for virus scanner: " + scanpath + ". FAIL!");
				return ERROR_CODES.MKDIR_FAILED.name();
			}
		}
		File scanpathFile = new File(scanpath + "/" + filename);
		if (!scanpathFile.exists()) {
			logger.warn("doVirusScan: " + scanpathFile.getAbsolutePath() + " doesn't exist");
			return ERROR_CODES.FILE_NOT_FOUND.name();
		}

		RunVirusScan virusScan = new RunVirusScan();
		virusScan.scan(scanpath + "/" + filename);
		logger.info("exitValue=" + virusScan.getExitValue());
		if (virusScan.getExitValue() > 0) {
			return virusScan.getOutput();
		}
		return EXIT_OK;
	}

	/**
	 * Returns the upload directory and the required permission for a specific upload command
	 * @param uploadCmd counterpart of the database field file_screener.uploadCmd
	 * @return String-Array, containing the file upload directory (String[0]) and the permission (String[1])
	 */
	@RequestMapping(value = "/uploadDirAndPermission.disp")
	@ResponseBody
	public String getUploadDirAndPermission(@RequestParam(required = true) String uploadCmd) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = ConnectionFactory.getConnection(ConnectionFactory.TRADEFINDER);
			if (conn == null) {
				logger.error("DB connection is null, database is probably down");
				return ERROR_CODES.DB_DOWN.name();
			}
			String queryStr = "SELECT PATTERN, PERMISSION FROM tradefinder_owner.file_screener "
					+ "WHERE upload_cmd = ? AND IS_DIRECTORY = 1";
			stmt = conn.prepareStatement(queryStr);
			stmt.setString(1, uploadCmd);
			rs = stmt.executeQuery();
			if (rs.next()) {
				String pattern = rs.getString("PATTERN");
				String permission = rs.getString("PERMISSION");
				return pattern + ";" + permission;
			} else {
				logger.error("getUploadDirAndPermission: "
						+ "There is no entry in the table FILE_SCREENER for upload_cmd=" + uploadCmd);
				return ERROR_CODES.CMD_NOT_FOUND.name();
			}
		} catch (SQLException sqle) {
			logger.error(sqle.getMessage());
		} catch (PMException e) {
			logger.error(e.getMessage());
		} finally {
			DBUtil.closeQuietly(conn, stmt, rs);
		}
		return EXIT_FAIL;
	}
	
		/**
	 * Returns the upload directory and the required permission for a specific upload command
	 * @return String-Array, containing the file upload directory (String[0]) and the permission (String[1])
	 */
	@RequestMapping(value = "/permissionToUploadCmd.disp")
	@ResponseBody
	public String getPermissionToUploadCmd() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = ConnectionFactory.getConnection(ConnectionFactory.TRADEFINDER);
			if (conn == null) {
				logger.error("DB connection is null, database is probably down");
				return ERROR_CODES.DB_DOWN.name();
			}
			
			List<String> permissions = getPermissions();
			String queryStr = "SELECT PERMISSION, UPLOAD_CMD, PATTERN FROM tradefinder_owner.file_screener "
					+ "WHERE permission IN (" + preparePlaceHolders(permissions.size()) + ") AND IS_DIRECTORY = 1";
			logger.info("queryStr=" + queryStr + ", permissions=" + permissions);
			stmt = conn.prepareStatement(queryStr);
			for (int i = 0; i < permissions.size(); i++) {
				stmt.setString(i + 1, permissions.get(i));
			}
			rs = stmt.executeQuery();
			
			StringBuilder sb = new StringBuilder();
			while (rs.next()) {
				String permission   = rs.getString("PERMISSION");
				String uploadCmd = rs.getString("UPLOAD_CMD");
				String uploadDir = rs.getString("PATTERN");
				String relativePath = uploadDir.substring(uploadDir.indexOf("file_upload/") + "file_upload/".length());
				if (!relativePath.endsWith("/"))
					relativePath += "/";
				sb.append(permission + ";" + uploadCmd + ";" + relativePath + ";");
			}
			if (sb.length() > 0) {
				return sb.toString();
			} else {
				logger.error("getPermittedUploadDirs: "
						+ "There is no UPLOAD_CMD entry in the table FILE_SCREENER where you have a permission");
				return ERROR_CODES.CMD_NOT_FOUND.name();
			}
		} catch (SQLException sqle) {
			logger.error(sqle.getMessage());
		} catch (PMException e) {
			logger.error(e.getMessage());
		} finally {
			DBUtil.closeQuietly(conn, stmt, rs);
		}
		return EXIT_FAIL;
	}
	
	public List<String> getPermissions() {
		List<String> permissions = new ArrayList<String>();
		Iterator<TFDetailedPermission> tfdps = TFSecurity.getTFUser().getAllMyPermissions().iterator();
		while (tfdps.hasNext()) {
			permissions.add(tfdps.next().getName());
		}
		return permissions;
	}
	
	/**
	 * Create a number of question marks, e.g. for bound variables in a prepared statement
	 * @param length 2 or higher
	 * @return "?,?,?" for length = 3
	 */
	public String preparePlaceHolders(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(i < length-1 ? "?," : "?");
		}
		return sb.toString();
	}

	/**
	 * Call a java method by reflection
	 *
	 * @param methodStr Java method, e.g. com.db.tradefinder.web.taglibs.TfUploadTag.hellox
	 * @return Returned object of the called method
	 */
	@RequestMapping(value = "/javaCallback.disp")
	@ResponseBody
	public String javaCallback(@RequestParam(required = true) String methodStr) {
		if (methodStr == null || methodStr.isEmpty()) {
			return null;
		}
		int lastDot = methodStr.lastIndexOf('.');
		if (lastDot == -1) {
			logger.error("FAIL in javaCallback: [" + methodStr + "] is not a valid name."
					+ "Ex. path.to.your.class.method. The method must be without parameters");
			return null;
		}
		String clazz = methodStr.substring(0, lastDot), method = methodStr.substring(lastDot + 1);
		logger.info("Name of class=" + clazz + ", method=" + method);

		try {
			Class c = Class.forName(clazz);
			Object o = c.newInstance();
			Method m = c.getDeclaredMethod(method, new Class[]{});
			Object mInvoke = m.invoke(o, new Object[]{});
			logger.info("method " + methodStr + " invoked");
			return mInvoke == null ? null : mInvoke.toString();
		} catch (ClassNotFoundException ex) {
			logger.error("FAIL in javaCallback: " + ex.getMessage());
		} catch (InstantiationException ex) {
			logger.error("FAIL in javaCallback: " + ex.getMessage());
		} catch (IllegalAccessException ex) {
			logger.error("FAIL in javaCallback: " + ex.getMessage());
		} catch (NoSuchMethodException ex) {
			logger.error("FAIL in javaCallback: " + ex.getMessage());
		} catch (InvocationTargetException ex) {
			logger.error("FAIL in javaCallback: " + ex.getMessage());
		}
		return null;
	}
}
