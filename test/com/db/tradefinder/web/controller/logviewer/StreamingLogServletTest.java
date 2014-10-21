package com.db.tradefinder.web.controller.logviewer;

import java.io.File;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for StreamingLogServlet
 * @author Bernhard
 */
public class StreamingLogServletTest {
	
	private static final int BYTES_COUNT = 1000;
	private static final int TEST_COUNT = 100;
	private static final int LINES_COUNT = 100;
	
	private final StreamingLogController servlet = new StreamingLogController();
	HttpSession session = null;

	public StreamingLogServletTest() {
		System.out.println(getClass().getSimpleName());
	}
	
	@Before public void setUp() {}
	@After 	public void tearDown() {}

	@Test
	public void testTailBytes() throws Exception {
		File logfile = new File(servlet.getLogPath(), servlet.getLogFile());
		long t1 = System.currentTimeMillis();
		
		servlet.tailBytes(logfile, null, BYTES_COUNT); servlet.tailBytes(logfile, session, BYTES_COUNT); // warmup
		for (int i = 0; i < TEST_COUNT; i++) {
			servlet.tailBytes(logfile, session, BYTES_COUNT);
		}
		System.out.printf("Duration %d x tailBytes(%d): %d ms\n", 
				TEST_COUNT, BYTES_COUNT, System.currentTimeMillis() - t1);
		String log = servlet.tailBytes(logfile, session, BYTES_COUNT);
		System.out.println(log == null ? "log=null!" : "log=\n" + servlet.postProcessLog(log, 1, null));

		assert(log != null && log.length() == BYTES_COUNT);
	}

	@Test
	public void testTailLines() throws Exception {
		System.out.println("tailLines");
		File logfile = new File(servlet.getLogPath(), servlet.getLogFile());
		long t1 = System.currentTimeMillis();
		
		servlet.tailLines(logfile, session, LINES_COUNT); servlet.tailLines(logfile, session, LINES_COUNT); // warmup
		for (int i = 0; i < 100; i++) {
			servlet.tailLines(logfile, session, LINES_COUNT);
		}
		System.out.printf("Duration %d x tailLines(%d): %d ms\n", 
				TEST_COUNT, LINES_COUNT, System.currentTimeMillis() - t1);
		String log = servlet.tailLines(logfile, session, LINES_COUNT);
		System.out.println(log == null ? "log=null!" : "log=\n" + servlet.postProcessLog(log, 1, null));
		
		assert(log != null);
	}
}