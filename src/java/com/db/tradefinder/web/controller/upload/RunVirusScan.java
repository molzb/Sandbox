package com.db.tradefinder.web.controller.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.util.tomcat.SystemInfo;

/**
 * Calls a command-line virus scanner
 * @author Bernhard
 */
public class RunVirusScan {

	private static final Logger logger = LoggerFactory.getLogger(RunVirusScan.class);
	private int exitValue = 0;
	private String output;

	public RunVirusScan() {
	}

	/**
	 * Scan a file on the file system
	 *
	 * @param filename Full path of a file, e.g. /export/apps/yourdir/virus.exe
	 * @return 0 = OK, 1 = VIRUS! (you can get the output of the virus scanner with {@link getOutput()}
	 */
	public int scan(String filename) {
		InputStream is = null;
		try {
			boolean isLocal = SystemInfo.isLocal();
			String dir = isLocal ? "/local/export/apps/tfrun/bin/" : "/export/apps/tfrun/bin/";
			String cmd = isLocal ? 
					"/local/export/apps/tfrun/bin/virusScanMock.bat " + filename : 
					"/export/apps/tfrun/bin/virusScanMock.sh " + filename;
			Process proc = Runtime.getRuntime().exec(cmd, null, new File(dir));

			proc.waitFor();

			is = proc.getInputStream();
			byte[] buf = new byte[is.available()];
			is.read(buf);

			exitValue = proc.exitValue();
			output = new String(buf);
			logger.info("RunVirusScan exit=" + exitValue + ", output=" + output);

			return getExitValue();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (InterruptedException ie) {
			logger.error(ie.getMessage(), ie);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			}
		}
		return -1;
	}

	public int getExitValue() {
		return exitValue;
	}

	public String getOutput() {
		return output;
	}
}
