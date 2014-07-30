package com.db.tradefinder.histsim.web;

import bm.scanner.DirWalker;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DirWalkerController {

	@RequestMapping("/dirwalk.disp")
	public ModelAndView dirwalk() {
		ModelAndView model = new ModelAndView("admin/dirwalk");
		model.getModel().put("list", new DirWalker().getHtmlList());
		return model;
	}
}
