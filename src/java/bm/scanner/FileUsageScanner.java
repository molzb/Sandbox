package bm.scanner;

import com.db.tradefinder.persistence.DBUtil;
import com.db.tradefinder.service.factory.ConnectionFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Erstelle die Referenzen (nach aussen und innen) von allen Dateien eines Verzeichnisses
 *
 * @author Bernhard
 */
public class FileUsageScanner {

	private static final Logger logger = Logger.getLogger(FileUsageScanner.class.getName());
	final static String context = "/sandbox/";
	final static String contextRealPath = "C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox/web/";
	public static final String[] filesToScan = new String[]{
		".xhtml", ".html", ".htm", ".js", ".jspf", ".jsp",
		".xml", ".ini", ".properties", ".tld",
		".java", ".pl", ".sql"/*, ".m" */};
	public static final String[] filesToDisplay = new String[]{"jpg", "png", "gif"};
	private FileUsageFilter fileFilter;
	private final List<File> files = new ArrayList<File>();
	private final List<FileModel> fileModels = new ArrayList<FileModel>();

	public static void main(String[] args) throws IOException {
		File rootDir = new File("C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox/");
		File thirdpartyDir = new File("C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox/build/web/WEB-INF/lib");
		FileUsageScanner scanner = new FileUsageScanner(rootDir);
		System.out.println(scanner.getPackagesFromJar(new File(thirdpartyDir, "spring-web-3.2.3.RELEASE.jar")).toString().replace(",", "\n"));
		List<JarFileModel> readJars = scanner.readJars(thirdpartyDir);
		System.out.println("JARS:\n-----");
		for (JarFileModel jar : readJars) {
			System.out.println(jar);
		}
		if (true) return;

		for (SpringMappingModel springMapping : scanner.springMappings) {
			System.out.println(springMapping);
		}
		System.out.println("Refs in DB: " + scanner.getReferencesInDatabase());

		scanner.printFileCountBySuffix();
		scanner.showFiles(".jsp");
		scanner.showFiles(".jspf");
	}
	private List<SpringMappingModel> springMappings;

	public FileUsageScanner() {
		logger.fine("FileUsageScanner()");	// just for Spring
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
					logger.info(beanDefinition.getBeanClassName());
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
									if (urlMapping.contains("//")) {
										urlMapping = urlMapping.replace("//", "/");
									}

									springMappingModel.addUrlMapping(urlMapping);

								}
							}
						}
					}

					springMappingModels.add(springMappingModel);

				} catch (ClassNotFoundException ex) {
					logger.log(Level.SEVERE, ex.getMessage(), ex);
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
			rs = stmt.executeQuery("SELECT ID, JSP_LINK, JSP_TEMPLATE FROM TF_MENU WHERE JSP_LINK IS NOT NULL "
					+ "ORDER BY JSP_TEMPLATE, JSP_LINK");
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

	private List<String> getStatsInDatabase() {
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
		fileFilter = new FileUsageFilter(filesToScan, true);
		if (rootDir.isDirectory()) {
			filterFiles(rootDir);
		} else {
			logger.severe("Can't scan " + rootDir.getAbsolutePath() + ", because it's not a directory");
		}
		addFilesToList();
//		makeReferences();	//TODO
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
				logger.info("# of " + suffix + " files: " + fileCount);
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
			if (file.getName().endsWith("_jsp.java"))
				continue;
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
				logger.info(file.toString());
			}
		}
	}

	private List<JarFileModel> readJars(File path) {
		if (!path.exists()) {
			logger.severe(path.getAbsolutePath() + " doesn't exist");
			return new ArrayList<JarFileModel>();
		}
		List<JarFileModel> jarFileModels = new ArrayList<JarFileModel>();
		File[] listFiles = path.listFiles();
		for (File file : listFiles) {
			if (file.getName().endsWith(".jar")) {
				JarFileModel jarFileModel = new JarFileModel(file);
				try {
					jarFileModel.setPackages(getPackagesFromJar(file));
				} catch (IOException ex) {
					logger.severe(ex.getMessage());
				}
				jarFileModels.add(jarFileModel);
			}
		}

		return jarFileModels;
	}

	/**
	 * Read through a jar and return all non-empty packages (that contain at least one class),
	 * e.g. commons-fileupload-1.2.1.jar -> org.apache.commons.fileupload, ...commons.fileupload.disk, ...
	 */
	private Set<String> getPackagesFromJar(File jarfile) throws IOException {
		if (!jarfile.exists()) {
			return new HashSet<String>();
		}
		List<String> classesInJar = new ArrayList<String>();
		JarInputStream jis = new JarInputStream(new FileInputStream(jarfile));
		JarEntry je;
		while ((je = jis.getNextJarEntry()) != null) {
			String entry = je.getName();
			if (entry.contains("META-INF")) {
				continue;
			}
			if (entry.endsWith(".class")) {
				classesInJar.add(entry);
			}
		}

		//Now get the packages out of the classnames (org/apache/web/MyClass.class -> org.apache.web)
		Set<String> filteredPackages = new HashSet<String>();
		for (String cl : classesInJar) {
			String myPackage = cl.substring(0, cl.lastIndexOf('/'));
			// convert 'org/apache/commons/' -> 'org.apache.commons'
			myPackage = myPackage.replace('/', '.');
			filteredPackages.add(myPackage);
		}
		classesInJar.clear();	//GC
		jis.close();

		return filteredPackages;
	}

	/**
	 * Scan java and jsp files for imports and return a list of unique package names, e.g.
	 * XYZ.java: import java.util.Date, java.util.List, java.sql.Statement -> java.util, java.sql
	 * @param filename e.g. abc.jsp, abc.jspf or Abc.java
	 * @param lines All lines of that java/jsp/jspf file
	 * @return A set of (unique) package names
	 */
	private Set<String> readImportsFromJavaAndJsp(String filename, List<String> lines) {
		Set<String> imports = new HashSet<String>();
		//JAVA: import com.db.tradefinder.service.factory.ConnectionFactory; import java.io.File;
		//JSP: <%@page import="java.util.Date"%><%@page import="java.util.Date, java.sql.SQLException"%>
		boolean isJsp = filename.endsWith(".jsp") || filename.endsWith(".jspf");
		boolean isJava = filename.endsWith(".java");
		for (String line : lines) {
			line = line.trim();
			if (isJsp) {
				if (line.startsWith("<%") && line.contains("page") && line.contains("import=")) {
					int iStart = line.indexOf("import=") + 8;
					int iEnd = line.indexOf("\"", iStart+1);
					String myImport = line.substring(iStart, iEnd);
					myImport = myImport.replace(" ", "");	//java.x.A, java.x.B -> java.x.A,java.x.B
					String[] importsInLine = myImport.split(",");
					System.out.println(filename + ": read import " + Arrays.toString(importsInLine));
					for (String pack : importsInLine) {
						pack = pack.substring(0, pack.lastIndexOf('.'));
						imports.add(pack);
					}
				}
			} else if (isJava) {
				if (line.startsWith("import ")) {
					String myImport = line.startsWith("import static ") ?
							line.substring("import static ".length(), line.length() - 1) :
							line.substring("import ".length(), line.length() - 1);
					System.out.println(filename + ": added import [" + myImport + "]");
					String pack = myImport.substring(0, myImport.lastIndexOf('.'));
					imports.add(pack);
				}
			}
		}
		return imports;
	}

	private FileModel readFile(File file) throws IOException {
		FileModel fileModel = new FileModel(file);
		String filename = file.getName();
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		fileModel.setLines(lines.size());

		removeLinesWithComments(lines, file.getName());

		if (filename.endsWith(".java") || filename.endsWith(".jsp") || filename.endsWith(".jspf")) {
			fileModel.setImports(readImportsFromJavaAndJsp(filename, lines));
			if (true) {
				return fileModel;
			}
		}
		
		for (String line : lines) {
			String lastSuffix = "";
			boolean lastSuffixFound = false;
			for (String suffix : filesToScan) {
				if (lastSuffix.contains(suffix) && lastSuffixFound) {
					continue;
				}
				if (line.contains(suffix)) {
					String reference = extractFilenameFromLine(line, suffix);
					if (reference != null && !reference.isEmpty()) {
						fileModel.getReferences().add(reference);
					}
					lastSuffixFound = true;
				}
				lastSuffix = suffix;
			}
		}
		return fileModel;
	}

	//TODO
	private void makeReferences() {
		for (FileModel file : fileModels) {
			for (FileModel f : fileModels) {
				if (file.getReferences().contains(f.getWebFilename())) {
					file.getReferencedBy().add(f.getWebFilename());
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
					String filename = line.substring(posFilename + 1, posOfSuffix + suffix.length());
					return filename.startsWith("http") || filename.startsWith("//")
							? null
							: context + filename;
				}
			}
			posFilename--;
		}

		return context + line.substring(0, posOfSuffix + suffix.length());
	}

	private void removeLinesWithComments(List<String> lines, String filename) {
		String suffix = filename.substring(filename.lastIndexOf('.') + 1);
		if (!suffix.equals("js") && !suffix.contains("jsp") && !suffix.equals("java")) {
			return;
		}
		boolean isJsp = suffix.contains("jsp");
		logger.info("removeLines in " + filename);
		int linesBefore = lines.size();
		Iterator<String> it = lines.iterator();
		if (suffix.equals("jsp")) {
			linesBefore += 0;
		}
		while (it.hasNext()) {
			switch (suffix) {
				case "js":
				case "jsp":
				case "jspf":
				case "java":
					String line = it.next().trim();
					if (line.startsWith("//")) {
						it.remove();
					}
					if (line.startsWith("/*") || (isJsp && line.startsWith("<%--")) ) {
						// einzeiliger Kommentar
						if (line.endsWith("*/") || (isJsp && line.endsWith("--%>")) ) {
							logger.info("remove " + line);
							it.remove();
						} else {	// mehrzeiliger Kommentar
							it.remove();
							for (;it.hasNext();) {
								line = it.next().trim();
								if (line.endsWith("*/") || (isJsp && line.endsWith("--%>")) ) {
									it.remove();
									break;
								}
								it.remove();
							}
						}
					}
					break;
			}
		}
		int linesAfter = lines.size();
		logger.info("#Lines before: " + linesBefore + ", #lines after removing comments: " + linesAfter);
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
