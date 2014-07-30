package com.db.tradefinder.histsim.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Bernhard
 */
public class FileUsageController {

	public FileUsageController() {
	}
	
	@RequestMapping("/fileusage.disp")
	@ResponseBody
	public String getFileUsageAsJSON(String foldeer) {
		StringBuilder sb = new StringBuilder("[");
		sb.append("]");
		return sb.toString();
	}
}
