package bm.scanner;

import com.db.tradefinder.persistence.DBUtil;
import com.db.tradefinder.service.factory.ConnectionFactory;
import com.kirkk.analyzer.Analyzer;
import com.kirkk.analyzer.framework.Jar;
import com.kirkk.analyzer.framework.bcelbundle.JarImpl;
import com.kirkk.analyzer.framework.bcelbundle.JarPackageImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
 * Erstelle die Referenzen (nach aussen und innen) von allen Dateien eines Verzeichnisses
 *
 * @author Bernhard
 */
public class FileUsageScanner {

	private static final Logger logger = Logger.getLogger(FileUsageScanner.class.getName());
	final static String context = "sandbox/";
	final static String contextRealPath = "C:\\Users\\Bernhard\\Documents\\NetBeansProjects\\Sandbox\\build\\web\\";
	public static final String[] filesToScan = new String[]{
		".xhtml", ".html", ".htm", ".js", ".jspf", ".jsp", ".css",	// Frontend
		".xml", ".ini", ".properties", ".tld",	// Config
		".java", ".pl", ".sql", ".m"};	// Backend
	public static final String[] filesToDisplay = new String[]{"jpg", "png", "gif"};
	private FileUsageFilter fileFilter;
	private final List<File> files = new ArrayList<File>();
	private final List<FileModel> fileModels = new ArrayList<FileModel>();

	// This is my own try. Works for most of the time, but we have false positives with LoggerFactory.getLogger, which is not a FQN
	private final Pattern fqnPattern = Pattern.compile("([a-z]\\w*\\.\\w+(\\.\\w+)*)[<\\( ;]");
//	http://stackoverflow.com/questions/5205339/regular-expression-matching-fully-qualified-java-classes
//	private final Pattern fqnPatternThr =  Pattern.compile("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
//	private final Pattern fqnPatternThr2 = Pattern.compile("([a-z][a-z_0-9]*\\.)*[A-Z_]($[A-Z_]|[\\w_])*");

	private List<SpringMappingModel> springMappings;
	private final List<String> javaPkgIgnoreList = Arrays.asList(new String[]{"java."});
	private final List<String> javaPkgAcceptList = Arrays.asList(new String[] {"javax.", "org.", "com."});

	public static void main(String[] args) throws Exception {
//		FileUsageScanner scan = new FileUsageScanner();
//		scan.getFQClassname("", "Logger logger = LoggerFactory.getLogger(\"test\");)");	// not a FQN
//		scan.getFQClassname("", "if(!com.db.TFSec.isPermitted(\"test\") return;");	// com.db.TFSec
//		scan.getFQClassname("", "new java.util.concurrent.BrokenException();");	// java.util.concurrent.BrokenException
//		scan.getFQClassname("", "java.util.Set<Log> loggers = new java.util.HashSet<>();");	// java.util.Set, java.util.HashSet
//		scan.getFQClassname("", "java.awt.Component c1d2 = new java.awt.List();");	// java.awt.Component, java.awt.List
//		scan.getFQClassname("", "com.de.tfsecurity.TFUser u;");	//com.de.tfsecurity.TFUser

//		System.out.println(scan.removeInlineComment("// das ist ein Kommentar"));
//		System.out.println(scan.removeInlineComment("/** Einzeiliger Kommentar*/ "));
//		System.out.println(scan.removeInlineComment("File[] f1 = FileUtils.EMPTY_FILE_ARRAY;	// Commons IO"));
//		System.out.println(scan.removeInlineComment("com.db.tradefinder.TradeFinderServlet /* com.db.dontReadThis */ tfs;	// and not that"));
//		File file = new File("/Users/Bernhard/Documents/NetBeansProjects/Sandbox/src/java/bm/scanner/FileUsageScanner.java");
//		File file = new File(contextRealPath, "fileUsageTest/test.jsp");
//		File file = new File(contextRealPath, "js/dirwalk.js");
//		List<String> lines = FileUtils.readLines(file, "UTF-8");
//		List<String> filteredLines = scan.removeLinesWithoutJavaCode(lines, file.getName());
//		for (String line : filteredLines) {
//			System.out.println(line);
//		}

		File rootDir = new File("C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox/");
		File thirdpartyDir = new File("C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox/build/web/WEB-INF/lib");
		FileUsageScanner scanner = new FileUsageScanner(rootDir);
		System.out.println(scanner.getPackagesFromJar(new File(thirdpartyDir, "spring-web-3.2.3.RELEASE.jar")).toString().replace(",", "\n"));
		List<FileModel> jarsList = scanner.readJarsWithJarAnalyzer(thirdpartyDir);
		scanner.persistFileModels(jarsList, false);
		System.out.println("JARS:\n-----");
		for (FileModel jar : jarsList) {
			System.out.println(jar);
		}
		
		for (SpringMappingModel springMapping : scanner.springMappings) {
			System.out.println(springMapping);
		}

		scanner.printFileCountBySuffix();
		scanner.showFiles(".jsp");
		scanner.showFiles(".jspf");
		scanner.showFiles(".css");
		
	}

	public FileUsageScanner() {
		logger.fine("FileUsageScanner()");	// just for Spring
	}

	public FileUsageScanner(File rootDir) {
		logger.fine("FileUsageScanner " + rootDir.getAbsolutePath());
		if (!rootDir.exists()) {
			JOptionPane.showMessageDialog(new JFrame(), rootDir + " doesn't exist");
			System.exit(1);
		}
		scan(rootDir);
	}
	
	public final void scan(File rootDir) {
		fileFilter = new FileUsageFilter(filesToScan, true);
		if (rootDir.isDirectory()) {
			filterFiles(rootDir);
		} else {
			logger.severe("Can't scan " + rootDir.getAbsolutePath() + ", because it's not a directory");
		}
		List<FileModel> fileModelList = readFiles();
		springMappings = scanSpringRequestMappings(fileModelList, new String[]{
			"com.db.tradefinder.histsim.web",
			"com.db.tradefinder.web.controller.upload",
			"com.db.tradefinder.web.controller.batchUser"
		});
		makeReferences();
		persistFileModels(fileModelList, true);
	}

	private List<SpringMappingModel> scanSpringRequestMappings(List<FileModel> fileModelList, 
			String[] controllerScanPath) {
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
					springMappingModel.setClassName(beanDefinition.getBeanClassName());
					
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
					
					for (FileModel m : fileModelList) {
						if (m instanceof JavaFileModel) {
							String clazzname = ((JavaFileModel)m).getClazzName();
							if (clazzname.equals(beanDefinition.getBeanClassName())) {
								if (!springMappingModel.getUrlMappings().isEmpty()) {
									String urlMappings = springMappingModel.getUrlMappings().toString();
									m.setWebFilename(urlMappings.substring(1, urlMappings.length()-1));
								}
							}
						}
					}

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
		List<String> refsInDB = new ArrayList<String>();
		try {
			conn = ConnectionFactory.getConnectionLocally();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, JSP_LINK, JSP_TEMPLATE FROM TF_MENU WHERE JSP_LINK IS NOT NULL "
					+ "ORDER BY JSP_TEMPLATE, JSP_LINK");
			while (rs.next()) {
				refsInDB.add(rs.getString("JSP_LINK"));
			}
		} catch (SQLException ex) {
			logger.info(ex.getMessage());
		} finally {
			DBUtil.closeQuietly(conn, stmt, rs);
		}
		return refsInDB;
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

	private List<FileModel> readFiles() {
		for (File file : files) {
			if (file.getName().endsWith("_jsp.java")) {
				continue;
			}
			try {
				FileModel fileModel = readFile(file);
				if (fileModel != null) {
					fileModels.add(fileModel);
				}
			} catch (IOException ioe) {
				logger.severe(ioe.getMessage());
			}
		}
		return fileModels;
	}

	private void showFiles(String suffix) {
		for (FileModel file : fileModels) {
			if (file.getMe().getName().endsWith(suffix)) {
				logger.info(file.toString());
			}
		}
	}
	
	private List<FileModel> readJarsWithJarAnalyzer(File pathWithJars) throws Exception {
		if (!pathWithJars.exists()) {
			logger.severe(pathWithJars.getAbsolutePath() + " doesn't exist");
			return new ArrayList<FileModel>();
		}
		// Run the test
		Analyzer analyzer = new Analyzer();
		Jar[] jarbundle = analyzer.analyze(pathWithJars);
		List<FileModel> jarFileModels = new ArrayList<FileModel>();
		for (Jar jar : jarbundle) {
			List<JarImpl> incomingDependencies = jar.getIncomingDependencies();
			List<JarImpl> outgoingDependencies = jar.getOutgoingDependencies();
			List<JarPackageImpl> packages = jar.getAllContainedPackages();
			List<String> incomingJarsList = new ArrayList<String>();
			for (JarImpl incoming : incomingDependencies) {
				incomingJarsList.add(incoming.getJarFileName());
			}
			List outgoingJarsList = new ArrayList<String>();
			for (JarImpl outgoing : outgoingDependencies) {
				outgoingJarsList.add(outgoing.getJarFileName());
			}
			Set<String> packagesList = new HashSet<String>();
			for (JarPackageImpl pack : packages) {
				packagesList.add(pack.getLongName());
			}
			String out = jar.getJarFileName() + ": \n\tIncoming " + incomingJarsList +
					"\n\tOutgoing " + outgoingJarsList +
					"\n\tExternally referenced " + jar.getAllExternallyReferencedPackages() +
					"\n\tUnidentifiable " + jar.getAllUnidentifiableExternallyReferencedPackages() +
					"\n\tPackages " + packagesList;
			System.out.println(out);
			
			File jarFile = new File(pathWithJars, jar.getJarFileName());
			JarFileModel jarFileModel = new JarFileModel(jarFile);
			jarFileModel.setPackages(packagesList);
			jarFileModel.setUsedBy(incomingJarsList);
			jarFileModel.setUses(outgoingJarsList);
			jarFileModel.setUnidentifiableExternalReferences(jar.getAllUnidentifiableExternallyReferencedPackages());
			jarFileModels.add(jarFileModel);
		}
		return jarFileModels;
	}

	@Deprecated
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
	 * Read through a jar and return all non-empty packages (that contain at least one class), e.g.
	 * commons-fileupload-1.2.1.jar -> org.apache.commons.fileupload, ...commons.fileupload.disk, ...
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
	 * Scan java and jsp files for imports and return a list of unique package names, e.g. XYZ.java: import
	 * java.util.Date, java.util.List, java.sql.Statement -> java.util, java.sql
	 *
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
					int iEnd = line.indexOf("\"", iStart + 1);
					String myImport = line.substring(iStart, iEnd);
					myImport = myImport.replace(" ", "");	//java.x.A, java.x.B -> java.x.A,java.x.B
					String[] importsInLine = myImport.split(",");
					System.out.println(filename + ": read import " + Arrays.toString(importsInLine));
					for (String pack : importsInLine) {
						pack = pack.substring(0, pack.lastIndexOf('.'));
						imports.add(pack);
					}
				} else {
					List<String> fqClassname = getFQClassname(filename, line);	//TODO
					if (fqClassname != null) {
						imports.addAll(fqClassname);
					}
				}
			} else if (isJava) {
				if (line.startsWith("import ")) {
					String myImport = line.startsWith("import static ")
							? line.substring("import static ".length(), line.length() - 1)
							: line.substring("import ".length(), line.length() - 1);
					System.out.println(filename + ": added import [" + myImport + "]");
					String pack = myImport.substring(0, myImport.lastIndexOf('.'));
					imports.add(pack);
				} else {
					List<String> fqClassname = getFQClassname(filename, line);
					if (fqClassname != null) {
						imports.addAll(fqClassname);
					}
				}
			}
		}
		return imports;
	}

	private List<String> getFQClassname(String filename, String line) {
		//TODO filtern mit Whitelist?
		if (1+1 == 2) {	//TODO entfernen
			return null;
		}
		if (!filename.endsWith("jsp") && !filename.endsWith("java") && !filename.endsWith("jspf")) {
			return null;
		}
		List<String> l = null;
		if (line != null && !line.isEmpty() && line.contains(".")) {
			Matcher matcher = fqnPattern.matcher(line);
			while (matcher.find()) {
				if (l == null) {
					l = new ArrayList<String>();
				}
				String fqClassname = matcher.group();
				// add classname, if not in blacklist AND in whitelist
				if (!inPackageList(fqClassname, javaPkgIgnoreList) && inPackageList(fqClassname, javaPkgAcceptList) ) {
					if (fqClassname.endsWith("(") || fqClassname.endsWith("<") || fqClassname.endsWith(" ")) {
						fqClassname = fqClassname.substring(0, fqClassname.length() - 1);
					}
					l.add(fqClassname);
				}
			}
			if (l != null) {
				System.out.println("Found FQN in " + line + " -> " + l);
			}
			return l;
		}
		return null;
	}

	private boolean inPackageList(String fqClassname, List<String> javaPackageList) {
		for (String pack : javaPackageList) {
			if (fqClassname.startsWith(pack)) {
				return true;
			}
		}
		return false;
	}
	
	private FileModel readFile(File file) throws IOException {
		FileModel fileModel;
		String filename = file.getName();
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		
		String suffix = filename.substring(filename.lastIndexOf('.') + 1);
		if (suffix.equals("js") || suffix.contains("jsp") || suffix.equals("java")) {
			lines = removeLinesWithoutJavaCode(lines, file.getName());
		}

		if (suffix.equals("java") || suffix.contains(".jsp")) {
			fileModel = new JavaFileModel(file);
		} else {
			fileModel = new FileModel(file);
		}
		fileModel.setLines(lines.size());
		
		if (suffix.equals("css")) {
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith("@import")) {
//				@import url("importedCss.css");
					int startIdx = line.indexOf("url(") + 5;
					int endIdx = line.indexOf('"', startIdx + 1);
					String url = line.substring(startIdx, endIdx);
					fileModel.getImports().add(url);
					fileModel.getUses().add(url);
				}
			}
			return fileModel;
		}

		if (filename.endsWith(".java") || filename.endsWith(".jsp") || filename.endsWith(".jspf")) {
			Set<String> imports = readImportsFromJavaAndJsp(filename, lines);
			fileModel.setImports(imports);
			if (imports.contains("java.lang.reflect")) {
				((JavaFileModel) fileModel).setUsesReflection(true);
			}
		}

		for (String line : lines) {
			line = line.trim();
			String lastSuffix = "";
			boolean lastSuffixFound = false;
			for (String suffi : filesToScan) {
				if (lastSuffix.contains(suffi) && lastSuffixFound) {
					continue;
				}
				if (fileModel instanceof JavaFileModel) {
					if (line.startsWith("package")) {
						String myPackage = line.substring(8, line.length() - 1);
						String filenameWithoutDotJava = filename.substring(0, filename.indexOf(".java"));
						((JavaFileModel)fileModel).setClazzName(myPackage + "." + filenameWithoutDotJava);
					}
				}
				if (line.contains(suffi)) {
					String reference = extractFilenameFromLine(line, suffi);
					if (reference != null && !reference.isEmpty()) {
						fileModel.getUses().add(reference);
					}
					lastSuffixFound = true;
				}
				lastSuffix = suffi;
			}
		}
		return fileModel;
	}

	//TODO
	private void makeReferences() {
		makeReferencesFromMenu();
		
		for (FileModel file : fileModels) {
			for (FileModel f : fileModels) {
				if (file.getUses().contains(f.getWebFilename())) {
					file.getUsedBy().add(f.getWebFilename());
				}
			}
		}
	}
	
	private void makeReferencesFromMenu() {
		List<String> refsInDatabase = getReferencesInDatabase();
		for (String ref : refsInDatabase) {
			for (FileModel fileModel : fileModels) {
				if (ref.equals(fileModel.getWebFilename())) {
					fileModel.setReferencedInMenu(true);
					System.out.println(fileModel.getWebFilename() + " is referenced in the database");
				}
			}
		}
		System.out.println("Refs in DB: " + refsInDatabase);
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

	private List<String> removeLinesWithoutJavaCode(List<String> lines, String filename) {
		String suffix = filename.substring(filename.lastIndexOf('.') + 1);
		if (!suffix.equals("js") && !suffix.contains("jsp") && !suffix.equals("java")) {
			return null;
		}
		List<String> filteredLines = new ArrayList<String>();
		boolean isJsp = suffix.contains("jsp");
		boolean isJava = suffix.equals("java");
		if (filename.equals("AdminUsers.jsp")) {
			filename = filename + "";
		}
		// Lines to keep in JSP(f):
		// <%@page import="org.springframework.http.HttpEntity"%>
		// <% ... everything, but comments %>
		// <jsp:useBean
		// <%@  include file
		// <jsp:include
		// <% java.code %>
		for (int i = 0; i < lines.size(); i++) {
			String line = removeInlineComment(lines.get(i).trim());
			// start remove comment 
			if (line.startsWith("/*") || (isJsp && line.startsWith("<%--"))) {
				for (; i < lines.size(); i++) {
					line = lines.get(i).trim();
					if (line.endsWith("*/") || (isJsp && line.endsWith("--%>"))) {
						break;
					}
				}
			} // end remove comment
			else if (!isJsp && !line.isEmpty()) {
				filteredLines.add(line);
			} else if (line.startsWith("<%") && line.contains("import=")) {
				// <%@page import="org.springframework.http.HttpEntity"%>
				filteredLines.add(line);
			} else if (line.startsWith("<jsp:useBean")) {
				filteredLines.add(line);
			} else if (line.startsWith("<%@") && line.contains("include") && line.contains("file=")) {
				// <%@  include file
				filteredLines.add(line);
			} else if (line.startsWith("<jsp:include")) {
				// <jsp:include
				filteredLines.add(line);
			} else if (line.startsWith("<%") && line.endsWith("%>")) {
				// one line java code
				filteredLines.add(line);
			} else if (line.startsWith("<%")) {	// multiline java code
				for (; i < lines.size(); i++) {
					line = removeInlineComment(lines.get(i).trim());
					if (line.isEmpty()) {
						continue;
					} else if (line.startsWith("/*")) {	// remove multiline comment
						for (; i < lines.size(); i++) {
							line = lines.get(i).trim();
							if (line.endsWith("*/")) {
								break;
							}
						}
					} // end remove multiline comment
					else {
						filteredLines.add(line);
						if (line.endsWith("%>")) {
							break;
						}
					}
				}
			}
		}	// not empty
		if (filename.equals("AdminUsers.jsp")) {
			for (String line : filteredLines) {
				System.out.println(line);
			}
		}
		return filteredLines;
	}

	private String removeInlineComment(String line) {
		line = line.trim();
		if (line.startsWith("/*") && line.endsWith("*/")) {
			return "";
		}
		if (line.startsWith("<%@") && line.endsWith("%>")) {
			return "";
		}
		if (line.startsWith("<%--") && line.endsWith("--%>")) {
			return "";
		}
		if (line.startsWith("//")) {
			return "";
		}
		boolean hasInnerComments = true;
		while (hasInnerComments) {
			if (line.contains("//")) {
				line = line.substring(0, line.indexOf("//")).trim();
			} else if (line.contains("/*") && line.contains("*/")) {
				int iStart = line.indexOf("/*");
				int iEnd = line.indexOf("*/");
				line = line.substring(0, iStart).concat(line.substring(iEnd + 2)).trim();
			} else {
				hasInnerComments = false;
			}
		}
		return line;
	}

	private void persistFileModels(List<FileModel> fileModels, boolean deleteBeforeInserting) {
		if (fileModels.isEmpty()) {
			return;
		}

		Connection conn = null;
		PreparedStatement stmtInsert = null;
		Statement stmtDelete = null;
		ResultSet rs = null;
		
		String sqlDeleteAll = "DELETE FROM fileusage";
		
		try {
			conn = ConnectionFactory.getConnectionLocally();
			
			if (deleteBeforeInserting) {
				stmtDelete = conn.createStatement();
				int deleteCount = stmtDelete.executeUpdate(sqlDeleteAll);
				logger.info("Deleted " + deleteCount + " fileusage objects in the database");
			}
			
			if (!(fileModels.get(0) instanceof JarFileModel)) {
				stmtInsert = prepareInsert(conn, stmtInsert, fileModels);
			} else {
				stmtInsert = prepareInsertJar(conn, stmtInsert, fileModels);
			}
			stmtInsert.executeBatch();
			conn.commit();
		} catch (SQLException ex) {
			logger.info(ex.getMessage());
		} finally {
			DBUtil.closeQuietly(conn, stmtInsert, rs);
		}
	}
	
	private PreparedStatement prepareInsert(Connection conn, PreparedStatement stmtInsert, List<FileModel> fileModels) throws SQLException {
		String sqlInsert =
				"INSERT INTO fileusage (filename, fullFilename, webFilename, "
				+ "imports, usesReflection, referencedInMenu, uses, usedBy) "
				+ "VALUES (?,?,?,?,?,?,?,?)";
		stmtInsert = conn.prepareStatement(sqlInsert);
		
		for (FileModel model : fileModels) {
			String imports = model.getImports().toString();
			String uses = model.getUses().toString();
			String usedBy = model.getUsedBy().toString();
			boolean usesReflection = model instanceof JavaFileModel && ((JavaFileModel)model).isUsesReflection();
			stmtInsert.setString(1, model.getMe().getName());
			stmtInsert.setString(2, model.getMe().getAbsolutePath());
			stmtInsert.setString(3, model.getWebFilename());
			stmtInsert.setString(4, imports.substring(1,imports.length()-1));
			stmtInsert.setBoolean(5, usesReflection);
			stmtInsert.setBoolean(6, model.isReferencedInMenu());
			stmtInsert.setString(7, uses.substring(1, uses.length()-1));
			stmtInsert.setString(8, usedBy.substring(1, usedBy.length()-1));

			stmtInsert.addBatch();
		}
		return stmtInsert;
	}

	private PreparedStatement prepareInsertJar(Connection conn, PreparedStatement stmtInsert, List<FileModel> fileModels) throws SQLException {
		String sqlInsert = 
				"INSERT INTO fileusage (filename, fullFilename, webFilename, imports, usesReflection, referencedInMenu, "
				+ "packages, unidentifiableExternalReferences) "
				+ "VALUES (?,?,'','',0,0,?,?)";
		stmtInsert = conn.prepareStatement(sqlInsert);
		
		for (int i = 0; i < fileModels.size(); i++) {
			JarFileModel model = (JarFileModel)fileModels.get(i);
			String packages = model.getPackages().toString();
			packages = packages.substring(1, packages.length()-1);
			String unpackages = model.getUnidentifiableExternalReferences().toString();
			unpackages = unpackages.equals("[]") ? "" : unpackages.substring(1, unpackages.length()-1);
			stmtInsert.setString(1, model.getMe().getName());
			stmtInsert.setString(2, model.getMe().getAbsolutePath());
			stmtInsert.setString(3, packages);
			stmtInsert.setString(4, unpackages);

			stmtInsert.addBatch();
		}
		return stmtInsert;
	}
}

@NoArgsConstructor
class SpringMappingModel {

	@Setter 
	private String className;
	@Setter
	private Class<?> declaringClass;
	@Getter
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
