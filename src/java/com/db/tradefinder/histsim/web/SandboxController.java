package com.db.tradefinder.histsim.web;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.db.tradefinder.histsim.domain.TradingDay;
import com.db.tradefinder.histsim.domain.TradingEntry;
import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Spring Controller for Sandbox
 *
 * @author Bernhard
 */
@Controller
public class SandboxController {

	private static final Logger logger = Logger.getLogger(SandboxController.class.getName());

	@Autowired
	ServletContext servletContext;

	@RequestMapping("/sandbox")
	public ModelAndView showSandboxPage() {
		ModelAndView model = new ModelAndView("riskMgmt/histsim/sandbox");

		try {
			String path = servletContext.getRealPath("protected/file_upload/histsim_var");
			CsvFileReader csvReader = new CsvFileReader(path);

			List<TradingEntry> lines;
			lines = csvReader.readCsvFile();
			List<TradingDay> days = csvReader.aggregateTradingEntries(lines);
			List<String> infos = csvReader.getInfoGeneral();
			model.getModel().put("days", days);
			model.getModel().put("jsonForTradingDays", csvReader.getJsonForTradingDays(days));
			model.getModel().put("colHasNonZeroValues", csvReader.getColHasNonZeroValues());
			model.getModel().put("distinctMonths", csvReader.getDistinctMonths());
			model.getModel().put("infoFoTradeId", infos.get(CsvFileReader.INFO_FO_TRADEID));
			model.getModel().put("infoValCcy", infos.get(CsvFileReader.INFO_VAL_CCY));
			model.getModel().put("infoUnitCode", infos.get(CsvFileReader.INFO_UNIT_CODE));
			model.getModel().put("infoCobDate", infos.get(CsvFileReader.INFO_COBDATE));
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return model;
	}

	@RequestMapping("/upload.disp")
	public void uploadCsv(HttpServletRequest request, PrintWriter out) {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		out.println("isMultipart=" + isMultipart);

		if (isMultipart) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				List items = upload.parseRequest(request);
				Iterator iterator = items.iterator();
				while (iterator.hasNext()) {
					FileItem item = (FileItem) iterator.next();

					if (!item.isFormField()) {
						String fileName = item.getName();

						File path = new File(servletContext.getRealPath("/protected/file_upload/histsim_var"));
						if (!path.exists()) {
							if (!path.mkdirs()) {
								logger.severe("Can't create path " + path + ". FAIL!");
								return;
							}
						}

						File uploadedFile = new File(path + "/" + fileName);
						out.println(uploadedFile.getAbsolutePath());
						item.write(uploadedFile);
					}
				}
			} catch (Exception e) {
				logger.severe(e.getMessage());
			}
		}
	}
}
