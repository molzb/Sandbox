package com.db.tradefinder.web.controller.batchUser;

import com.db.tradefinder.persistence.DBUtil;
import com.db.tradefinder.persistence.PMException;
import com.db.tradefinder.service.factory.ConnectionFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "batchUser")
public class BatchUserController {

	private static final Logger logger = LoggerFactory.getLogger(BatchUserController.class);
	private	final String uploadPath = "/export/apps/tradefnd/tomcat/webapps/tradefnd/protected/file_upload/userbatch/";

	private static final String RET_OK = "OK", RET_FAIL = "FAIL";

	@Autowired
	HttpSession session;

	public static void main(String[] args) {
        BatchUserController batchUserServlet = new BatchUserController();
//        logger.info(batchUserServlet.getJSON("35085-1_20140415_leaver.csv"));
		batchUserServlet.testBatchComparator();
	}
	
	BatchUserController() {}

    /**
	 * Reads the CSV file and converts it into a list of user objects (of type {@link SingleUserInBatch} )
	 * @return List of user objects
	 */
	private List<SingleUserInBatch> readCsv(String csvFilename) {
		try {
			List<String> lines = FileUtils.readLines(new File(uploadPath + csvFilename), "UTF-8");
			logger.info("#users in CSV=" + (lines.size()-1));
			SingleUserReader userReader = new SingleUserReader();
            userReader.parseHeader(lines.get(0));
			List<SingleUserInBatch> users = new ArrayList<SingleUserInBatch>(lines.size() - 1);
			for (int i = 1; i < lines.size(); i++) {
				String line = lines.get(i);
				try {
					SingleUserInBatch user = userReader.parseUser(line);
					users.add(user);
				} catch (ParseException pex) {
					logger.error(pex.getMessage(), pex);
				}
			}
            return users;
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
        return null;
	}

    /**
	 * Returns a list with all objects ('users' as JSON objects) that were in the CSV or zip file
	 * @param csvFilename filename of the CSV that is inside the uploaded zip file, e.g. 35085-1_20140415_leaver.csv
	 * @return list with JSON objects
	 */
	@RequestMapping(value = "batchUserJson.disp", produces="application/json")
    @ResponseBody
    public String getJSON(@RequestParam String csvFilename) {
        List<SingleUserInBatch> users = readCsv(csvFilename);
		if (session != null) {
			session.setAttribute("batchUsers", users);
		}

        StringBuilder sb = new StringBuilder("[");
        for (SingleUserInBatch user : users) {
            sb.append(user.toJSON()).append(",\n");
        }
        sb.delete(sb.length()-2, sb.length());
        sb.append("]");
        return sb.toString();
    }

	/**
	 * Unzips the uploaded file that contains a CSV file (with the users that have to be locked in the database)
	 * @param filename Name of the Zip file containing a CSV file, e.g. 35085_leaver.csv.zip
	 * @return Name of the CSV file inside the zip file, e.g. 35085_leaver.csv 
	 * @throws IOException 
	 */
	@RequestMapping(value = "unzipUploadedCsv.disp")
    @ResponseBody
    public final String unzipCsv(@RequestParam String filename) throws IOException {
		File inputFile = new File(uploadPath + filename);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(inputFile));
		ZipEntry ze;
		String csvFilename = "";
		while ((ze = zis.getNextEntry()) != null) {
			csvFilename = ze.getName();
			File outputFile = new File(uploadPath + csvFilename);
			if (outputFile.exists()) {
				logger.warn("File " + outputFile.getAbsolutePath() + " already exists.");
				logger.warn("Probably the file " + inputFile.getAbsolutePath() + " has already been unzipped");
			}
			FileOutputStream fout = new FileOutputStream(uploadPath + ze.getName());
			for (int c = zis.read(); c != -1; c = zis.read()) {
				fout.write(c);
			}
			zis.closeEntry();
			fout.close();
		}
		zis.close();
		return csvFilename;
	}

	/**
	 * Set all the users to "locked", i.e. do a sql UPDATE on TRADEFINDER_USERS
	 * @return OK or FAIL
	 */
	@RequestMapping(value = "lockUsersInDB.disp")
	@ResponseBody
	public final String lockUsersInDB() {
		List<SingleUserInBatch> users = (List<SingleUserInBatch>)session.getAttribute("batchUsers");
		logger.info("#users in session object: " + users.size());

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = ConnectionFactory.getConnection(ConnectionFactory.TRADEFINDER);
			conn.setAutoCommit(false);
			String queryStr = "UPDATE tradefinder_owner.TF_TRADEFINDER_USERS"
					+ " SET LOCKED=1, REASON=? WHERE USERNAME=?";

			stmt = conn.prepareStatement(queryStr);
			for (SingleUserInBatch user : users) {
				stmt.setString(1, user.getReason());
				stmt.setString(2, user.getAccount());
				stmt.addBatch();
			}
			if (stmt != null) {
				stmt.executeBatch();
				conn.commit();
			}
		} catch (SQLException sqle) {
			try {
				logger.error(sqle.getMessage(), sqle);
				if (conn != null) {
					conn.rollback();
				}
				return RET_FAIL;
			} catch (SQLException ex) {
				logger.error(ex.getMessage(), ex);
			}
		} catch (PMException pme) {
			logger.error(pme.getMessage(), pme);
		} finally {
			DBUtil.closeQuietly(conn, stmt, null);
		}

		return RET_OK;
	}

	private void testBatchComparator() {
		List<String> strs = Arrays.asList(new String[] {"eins", "zwei", "drei"});
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(strs, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2) > 0 ? 1 : -1;
			}
		});
		System.out.println(strs);
	}
}
