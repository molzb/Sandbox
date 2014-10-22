package com.db.tradefinder.web.controller.logviewer;

import java.io.File;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for StreamingLogServlet
 * @author Bernhard
 */
public class StreamingLogControllerTest {
	
	private static final int BYTES_COUNT = 1000;
	private static final int TEST_COUNT = 100;
	private static final int LINES_COUNT = 100;
	
	private final StreamingLogController controller = new StreamingLogController();
	HttpSession mockSession = new MockSession(null);

	public StreamingLogControllerTest() {
		System.out.println(getClass().getSimpleName());
	}
	
	@Before public void setUp() {
		System.out.println("--------------- setup ----------------");
		controller.setSession(mockSession);
	}
	@After 	public void tearDown() {}

	@Test
	public void testTailBytes() throws Exception {
		File logfile = new File(controller.getLogPath(), controller.getLogFile());
		long t1 = System.currentTimeMillis();
		
		controller.tailBytes(logfile, null, BYTES_COUNT); controller.tailBytes(logfile, mockSession, BYTES_COUNT); // warmup
		for (int i = 0; i < TEST_COUNT; i++) {
			controller.tailBytes(logfile, mockSession, BYTES_COUNT);
		}
		System.out.printf("Duration %d x tailBytes(%d): %d ms\n", 
				TEST_COUNT, BYTES_COUNT, System.currentTimeMillis() - t1);
		String log = controller.tailBytes(logfile, mockSession, BYTES_COUNT);
		System.out.println(log == null ? "log=null!" : "log=\n" + controller.postProcessLog(log, 1, null));

		assert(log != null && log.length() == BYTES_COUNT);
	}

	@Test
	public void testTailLines() throws Exception {
		System.out.println("tailLines");
		File logfile = new File(controller.getLogPath(), controller.getLogFile());
		long t1 = System.currentTimeMillis();
		
		controller.tailLines(logfile, mockSession, LINES_COUNT); controller.tailLines(logfile, mockSession, LINES_COUNT); // warmup
		for (int i = 0; i < 100; i++) {
			controller.tailLines(logfile, mockSession, LINES_COUNT);
		}
		System.out.printf("Duration %d x tailLines(%d): %d ms\n", 
				TEST_COUNT, LINES_COUNT, System.currentTimeMillis() - t1);
		String log = controller.tailLines(logfile, mockSession, LINES_COUNT);
		System.out.println(log == null ? "log=null!" : "log=\n" + controller.postProcessLog(log, 1, null));
		
		assert(log != null);
	}
	
	@Test
	public void testWordIndexOf() {
		String s = "ERROR 03:00:34 com.db.tradefinder.web.controller.logviewer.StreamingLogController  - TestException ";
		if (s.contains("com.db.tradefinder")) {
			int[] startEnd = controller.wordIndexOf(s, "com.db.tradefinder");
			String start = s.substring(0, startEnd[0]);
			s = start + "<b>" + s.substring(startEnd[0], startEnd[1]) + "</b>" + s.substring(startEnd[1] + 1);
		}
		System.out.println("s=" + s);
	}
}

class MockSession extends StandardSession {

	HashMap<String, Object> map = new HashMap<String, Object>();
	
	public MockSession(Manager manager) {
		super(null);
	}

	@Override
	public void setAttribute(String name, Object value) {
		map.put(name, value);
	}
	public Object getAttribute(String name) {
		return map.get(name);
	}
}