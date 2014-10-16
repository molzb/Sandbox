package de.test.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;

/**
 * Servlet implementation class StreamingLogServlet
 */
@WebServlet("/StreamingLogServlet")
public class StreamingLogServlet extends HttpServlet {

	private static final long serialVersionUID = 12102014L;
	private static final Logger  logger = Logger.getLogger(StreamingLogServlet.class.getName());

	private final String[] SPACES = {"", "&nbsp;", "&nbsp;&nbsp;", "&nbsp;&nbsp;&nbsp;", "&nbsp;&nbsp;&nbsp;&nbsp;",
		"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"};
	private final String _SPAN = "<span>", SPAN_ = "</span>";
//	String logPath = "C:/Users/molzber/AppData/Local/";
//	String logFile = ""tomcat_base.log"";
	private String logPath = "C:/xampp/tomcat/logs/";
	private String logFile = "catalina.2014-10-15.log";
	private final int MAX_FILELEN_DIFF = 10000;
	
	Thread writeLogThread;
	
	public static void main(String[] args) {
		GregorianCalendar gc = new GregorianCalendar(2014, 2, 30);
		System.out.println("gc=" + gc.getTime());
		gc.roll(Calendar.MONTH, -1);
		System.out.println("gc=" + gc.getTime());
	}
	
	private void testLogWriteThread() {
		if (writeLogThread != null && writeLogThread.isAlive()) 
			writeLogThread.stop();
		
		writeLogThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					int rnd = (int)(Math.random() * 3.0);
					switch (rnd) {
						case 0: logger.info(new Date().toString()); break;
						case 1: logger.warn(new Date().toString()); break;
						case 2: logger.error(new Date().toString()); break;
					}
					try { Thread.sleep(3000); } catch (Exception e) {}
				}
			}
		};
		writeLogThread.start();
	}

	private String getSpaces(int numGreater0, int length) {
		int cipherLen = (int) Math.log10(numGreater0) + 1;
		return SPACES[length - cipherLen];
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("doGet " + request.getParameterMap());
		testLogWriteThread();
		response.setContentType("text/html;charset=UTF-8");
		
		HttpSession session = request.getSession(true);

		File logfile = new File(logPath, logFile);
		boolean clearParameter = ServletRequestUtils.getBooleanParameter(request, "clear", false);
		int linesParameter = ServletRequestUtils.getIntParameter(request, "lines", 0);
		int bytesParameter = ServletRequestUtils.getIntParameter(request, "bytes", 0);
		
		if (clearParameter == true) {
			session.setAttribute("lastLine", 1);
			return; 			
		}
		
		boolean bytesDiffParameter = ServletRequestUtils.getBooleanParameter(request, "bytesDiff", false);
		if (bytesDiffParameter) {
			bytesParameter = getFileLenDiff(logfile, session);
			if (bytesParameter == 0) {
				logger.warn("FileLenDiff = 0. That means, that there are no file len changes compared to the last time");
			}
		}
		
		int lastLineParameter = session.getAttribute("lastLine") != null
				? (Integer) session.getAttribute("lastLine")
				: ServletRequestUtils.getIntParameter(request, "lastLine", 1);
		
		if (linesParameter > 0 && bytesParameter > 0)
			throw new IllegalArgumentException("The request parameter 'bytes', 'lines' cannot BOTH be greater 0. " +
					"You can EITHER read a number of bytes OR a number of lines.");
		if (linesParameter == 0 && bytesParameter == 0)
			throw new IllegalArgumentException("ONE of the request parameter 'bytes' and 'lines' MUST be greater 0 " +
					"OR 'bytesDiff' must be set to true. ");

		String log = null;
		if (linesParameter > 0) {
			session.setAttribute("lastLine", 1);
			log = tailLines(logfile, session, linesParameter);
		} else if (bytesParameter > 0) {
			log = tailBytes(logfile, session, bytesParameter);
		}
		
		PrintWriter out = response.getWriter();
		out.println(postProcessLog(log, lastLineParameter, session));
	}
	
	public String postProcessLog(String log, int lineNumber, HttpSession session) {
		String[] lines = log.split("\\n"); // System.getProperty("line.separator"));
		logger.info("postProcess " + lines.length + " lines");
		StringBuilder sbFormatted = new StringBuilder(log.length());
		String BR = "<br/>";

		for (String line : lines) {
			line = StringEscapeUtils.escapeHtml(line);
			if (line.contains("Exception")) {
				int idx = line.indexOf("Exception");
				String start = line.substring(0, idx);
				line = start + "<span class='exc'>Exception</span>" + line.substring(idx + 9);
			} else if (line.contains("ERROR")) {
				int idx = line.indexOf("ERROR");
				String start = line.substring(0, idx);
				line = start + "<span class='error'>ERROR</span>" + line.substring(idx + 5);
			} else if (line.contains("WARN")) {
				int idx = line.indexOf("WARN");
				String start = line.substring(0, idx);
				line = start + "<span class='warn'>WARN</span>" + line.substring(idx + 4);
			} else if (line.contains("INFO")) {
				int idx = line.indexOf("INFO");
				String start = line.substring(0, idx);
				line = start + "<span class='info'>INFO</span>" + line.substring(idx + 4);
			}
			sbFormatted.append(_SPAN).append(getSpaces(lineNumber, 5) + lineNumber + " | ");
			sbFormatted.append(line).append(BR).append(SPAN_);
			lineNumber++;
		}
		
		if (session != null)
			session.setAttribute("lastLine", lineNumber);

		return sbFormatted.toString();
	}
	
	public int getFileLenDiff(File file, HttpSession session) throws IOException {
		long actualLength = file.length();
		if (session != null) {	// request can be null, when we're running a JUnit test
			long lastKnownLength = session.getAttribute("lastKnownLength") == null
					? 0 : (long) session.getAttribute("lastKnownLength");
			logger.debug("lastKnownLength=" + lastKnownLength);
			session.setAttribute("lastKnownLength", actualLength);
			int fileLenDiff = (int)(actualLength - lastKnownLength);
			logger.debug("fileLenDiff=" + Math.min(fileLenDiff, MAX_FILELEN_DIFF));
			return Math.min(fileLenDiff, MAX_FILELEN_DIFF);
		}
		return 0;
	}

	public String tailBytes(File file, HttpSession session, int bytes) throws IOException {
		logger.info("Read " + bytes + " bytes from " + file.toString());
		return tail(file, session, 0, bytes);
	}

	public String tailLines(File file, HttpSession session, int lines) throws IOException {
		logger.info("Read " + lines + " lines from " + file.toString());
		return tail(file, session, lines, 0);
	}

	private String tail(File file, HttpSession session, int lines, int bytes) throws IOException {
		if (lines > 0 && bytes > 0) {
			throw new IllegalArgumentException("You cannot set both bytes>0 and lines>0");
		}

		FileInputStream fileInputStream = new FileInputStream(file);
		
		FileChannel channel = fileInputStream.getChannel();
		session.setAttribute("lastKnownLength", channel.size());
		
		try {
			// Read a number of bytes
			if (bytes > 0) {
				ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, channel.size() - bytes, bytes);
				byte[] buf = new byte[bytes];
				buffer.get(buf);
				return new String(buf);
			}

			// Read a number of lines
			ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			buffer.position((int) channel.size());
			int count = 0;
			StringBuilder sb = new StringBuilder((int) channel.size());

			for (long i = channel.size() - 1; i >= 0; i--) {
				char c = (char) buffer.get((int) i);
				sb.append(c);

				if (c == '\n') {
					if (count == lines) {
						break;
					}
					count++;
				}
			}
			sb.reverse();
			sb.insert(0, " ------------- THE FILE HAS BEEN TRUNCATED --------------\n");

			return sb.toString();
		} finally {
			channel.close();
			fileInputStream.close();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		doGet(request, response);
	}

	public String getLogPath() {
		return logPath;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
}
