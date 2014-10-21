package com.db.tradefinder.histsim.web;

import bm.scanner.DirWalker;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DirWalkerController {

	@RequestMapping("/dirwalk.disp")
	public ModelAndView dirwalk() {
		ModelAndView model = new ModelAndView("admin/dirwalk");
		model.getModel().put("list", new DirWalker().getHtmlList());
		return model;
	}
	
	@RequestMapping(value = "/filesInDir.action", produces = "application/json")
	@ResponseBody
	public String getFilesInDir(@RequestParam String filename) {
		return "{ \"filename\": \"" + filename + "\" }";
	}

	@RequestMapping(value = "/fileDetails.action", produces = "application/json")
	@ResponseBody
	public String getFileDetails(@RequestParam String filename) {
		return "{ \"filename\": \"" + filename + "\" }";
	}

}
