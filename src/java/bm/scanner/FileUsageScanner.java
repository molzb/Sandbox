package bm.scanner;

import com.db.tradefinder.persistence.DBUtil;
import com.db.tradefinder.service.factory.ConnectionFactory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Durchsuche die 
 * @author Bernhard
 */
public class FileUsageScanner {

	private static final Logger logger = Logger.getLogger(FileUsageScanner.class.getName());
	final static String context = "/sandbox/";
	final static String contextRealPath = "C:\\Users\\Bernhard\\Documents\\NetBeansProjects\\Sandbox\\web\\";
	private final String[] filesToScan = new String[]{
		".xhtml", ".html", ".htm", ".jspf", ".jsp", ".js",
		".xml", ".ini", ".properties",
		".java", ".pl", ".sql"/*, ".m" */};
	private FileUsageFilter fileFilter;
	private final List<File> files = new ArrayList<File>();
	private final List<FileModel> fileModels = new ArrayList<FileModel>();

	public static void main(String[] args) {
		File rootDir = new File("C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox/web");
		FileUsageScanner scanner = new FileUsageScanner(rootDir);
		for (SpringMappingModel springMapping : scanner.springMappings) {
			System.out.println(springMapping);
		}
		System.out.println(scanner.getReferencesInDatabase());
		scanner.printFileCountBySuffix();
		scanner.showFiles(".jsp");
//		scanner.showFiles(".jspf");
	}
	private List<SpringMappingModel> springMappings;

	public FileUsageScanner() {
		// just for Spring 
		System.out.println("FileUsageScanner()");
	}

	public FileUsageScanner(File rootDir) {
		if (!rootDir.exists()) {
			JOptionPane.showMessageDialog(new JFrame(), rootDir + " doesn't exist");
			System.exit(1);
		}
		scan(rootDir);
	}

	private List<SpringMappingModel> scanSpringRequestMappings(String[] controllerScanPath) {
		List<SpringMappingModel> springMappingModels = new ArrayList<SpringMappingModel>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
		for (String scanPath : controllerScanPath) {
			for (BeanDefinition beanDefinition : scanner.findCandidateComponents(scanPath)) {
				try {
					System.out.println(beanDefinition.getBeanClassName());
					Class<?> cl = Class.forName(beanDefinition.getBeanClassName());
					Annotation[] annotations = cl.getDeclaredAnnotations();
					String[] classMappings = new String[]{};
					String requestMappingClassName = RequestMapping.class.getSimpleName();
					SpringMappingModel springMappingModel = new SpringMappingModel();
					springMappingModel.setDeclaringClass(cl);
					for (Annotation classAnnotation : annotations) {
						if (classAnnotation.annotationType().getSimpleName().equals(requestMappingClassName)) {
							classMappings = ((RequestMapping) classAnnotation).value();
							break;
						}
					}
					if (classMappings.length == 0) {
						classMappings = new String[]{""};
					}

					Method[] methods = cl.getDeclaredMethods();
					for (Method method : methods) {
						RequestMapping reqMapping = method.getAnnotation(RequestMapping.class);
						if (reqMapping != null) {
							for (String classMapping : classMappings) {
								for (String methodMapping : reqMapping.value()) {
									if (!methodMapping.startsWith("/")) {
										methodMapping = "/" + methodMapping;
									}
									if (!methodMapping.endsWith(".disp")) {
										methodMapping += ".disp";
									}
									String urlMapping = FileUsageScanner.context + classMapping + methodMapping;
									if (urlMapping.contains("//"))
										urlMapping = urlMapping.replace("//", "/");

									springMappingModel.addUrlMapping(urlMapping);

								}
							}
						}
					}

					springMappingModels.add(springMappingModel);

				} catch (ClassNotFoundException ex) {
					logger.log(Level.SEVERE, null, ex);
				}
			}
		}
		return springMappingModels;
	}
	
	private List<String> getReferencesInDatabase() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionFactory.getConnectionLocally();
			List<String> refsInDB = new ArrayList<String>();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, JSP_LINK, JSP_TEMPLATE FROM TF_MENU ORDER BY JSP_TEMPLATE, JSP_LINK");
			while (rs.next()) {
				refsInDB.add(rs.getString("JSP_LINK"));
			}
			return refsInDB;
		} catch (SQLException ex) {
			logger.info(ex.getMessage());
		} finally {
			DBUtil.closeQuietly(conn, stmt, rs);
		}
		return new ArrayList<String>();
	}

	public final void scan(File rootDir) {
		fileFilter = new FileUsageFilter(filesToScan);
		if (rootDir.isDirectory()) {
			filterFiles(rootDir);
		} else {
			logger.severe("Can't scan " + rootDir.getAbsolutePath() + ", because it's not a directory");
		}
		addFilesToList();
		makeReferences();
		springMappings = scanSpringRequestMappings(new String[]{
			"com.db.tradefinder.histsim.web",
			"com.db.tradefinder.web.controller.upload",
			"com.db.tradefinder.web.controller.batchUser"
		});
	}

	private void printFileCountBySuffix() {
		for (String suffix : filesToScan) {
			int fileCount = 0;
			for (File file : files) {
				if (file.getName().endsWith(suffix)) {
					fileCount++;
				}
			}
			if (fileCount > 0) {
				System.out.println("# of " + suffix + " files: " + fileCount);
			}
		}
	}

	public final List<File> filterFiles(File rootDir) {
		if (rootDir.isDirectory()) {
			File[] listFiles = rootDir.listFiles(fileFilter);
			for (File file : listFiles) {
				if (file.isFile()) {
					files.add(file);
				} else {
					filterFiles(file);
				}
			}
		}
		return files;
	}

	private void addFilesToList() {
		for (File file : files) {
			try {
				FileModel fileModel = readFile(file);
				fileModels.add(fileModel);
			} catch (IOException ioe) {
				logger.severe(ioe.getMessage());
			}
		}
	}

	private void showFiles(String suffix) {
		for (FileModel file : fileModels) {
			if (file.getMe().getName().endsWith(suffix)) {
				System.out.println(file);
			}
		}
	}

	private FileModel readFile(File file) throws IOException {
		FileModel fileModel = new FileModel(file);
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		fileModel.setLines(lines.size());
		for (String line : lines) {
			String lastSuffix = "";
			boolean lastSuffixFound = false;
			for (String suffix : filesToScan) {
				if (lastSuffix.contains(suffix) && lastSuffixFound) {
					continue;
				}
				if (line.contains(suffix)) {
					fileModel.references.add(extractFilenameFromLine(line, suffix));
					lastSuffixFound = true;
				}
				lastSuffix = suffix;
			}
		}
		return fileModel;
	}

	private void makeReferences() {
		for (FileModel file : fileModels) {
			for (FileModel f : fileModels) {
				if (file.references.contains(f.getWebFilename())) {
					file.referencedBy.add(f.getWebFilename());
				}
			}
		}
	}

	private String extractFilenameFromLine(String line, String suffix) {
		char[] delimiters = new char[]{'\"', '\'', ',', ';'};
		line = line.trim();
		int posFilename = line.indexOf(suffix);
		int posOfSuffix = line.indexOf(suffix);

		while (posFilename > 0) {
			for (char c : delimiters) {
				if (line.charAt(posFilename) == c) {
					return context + line.substring(posFilename + 1, posOfSuffix + suffix.length());
				}
			}
			posFilename--;
		}

		return context + line.substring(0, posOfSuffix + suffix.length());
	}
}

@AllArgsConstructor
class FileUsageFilter implements FileFilter {

	private final String[] filesToScan;

	@Override
	public boolean accept(File pathname) {
		for (String suffix : filesToScan) {
			if (pathname.isDirectory() || pathname.getName().endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

}

class FileModel {

	FileModel(File me) {
		this.me = me;
		webFilename = me.getAbsolutePath().replace(FileUsageScanner.contextRealPath, FileUsageScanner.context);
		webFilename = webFilename.replace('\\', '/').replace("//", "/");
	}

	@Getter
	@Setter
	File me;
	@Getter
	@Setter
	String webFilename;
	@Getter
	@Setter
	List<String> referencedBy = new ArrayList<String>();
	@Getter
	@Setter
	List<String> references = new ArrayList<String>();
	@Getter
	@Setter
	int lines;

	@Override
	public String toString() {
		return webFilename + " (lines: " + lines + ")\n\tReferences=" + references + "\n\tReferencedBy=" + referencedBy;
	}
}

@NoArgsConstructor
class SpringMappingModel {

	@Setter
	private Class<?> declaringClass;
	private final List<String> urlMappings = new ArrayList<String>();

	public void addUrlMapping(String urlMapping) {
		urlMappings.add(urlMapping);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Spring-Controller: " + declaringClass.getSimpleName() + "\n");
		for (String urlMapping : urlMappings) {
			sb.append("\t" + urlMapping + "\n");
		}
		return sb.toString();
	}
}
