package bm.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic File model
 * @see JarFileModel
 * @see JavaFileModel
 * @author Bernhard
 */
public class FileModel {

	public FileModel(File me) {
		this.me = me;
		File contextRealpath = new File(FileUsageScanner.CONTEXT_REALPATH);
		if (me.getAbsolutePath().contains(contextRealpath.getPath()) && !me.getAbsolutePath().contains("WEB-INF")) {
			webFilename = me.getAbsolutePath().replace(contextRealpath.getPath(), FileUsageScanner.CONTEXT);
			webFilename = webFilename.replace('\\', '/').replace("//", "/");
			if (webFilename.startsWith("/"))
				webFilename = webFilename.substring(1);
		} else {
			webFilename = "";
		}
	}

	@Getter
	@Setter
	private File me;
	@Getter
	@Setter
	private String webFilename;
	@Getter
	@Setter
	private List<String> usedBy = new ArrayList<>();
	@Getter
	@Setter
	private List<String> uses = new ArrayList<>();
	@Getter
	@Setter
	private int lines;
	@Getter
	@Setter
	private Set<String> imports = new HashSet<>();
	@Getter
	@Setter
	private boolean referencedInMenu = false;
	@Getter
	@Setter
	private boolean usesReflection = false;

	private List<String> getRefsBySuffix(String suffix) {
		List<String> l = new ArrayList<>();
		for (String ref : usedBy) {
			if (ref.endsWith(suffix)) {
				l.add(ref);
			}
		}
		return l;
	}
	
	@Override
	public String toString() {
		return webFilename + " (lines: " + lines + ")\n\tUses=" + uses + "\n\tUsedBy=" + usedBy;
	}

	public String toJSON() {
		return String.format("{ \"filename\": \"%s\", " + 
				" \"webfilename\": \"%s\", " + 
				" \"refByJava\": \"%s\", ", 
				" \"refByJSP\": \"%s\", ", 
				" \"refBySpring\": \"%s\", ", 
				" \"refByMenu\": \"%s\" }, ", 
				me.getName(), webFilename, 
				getRefsBySuffix("java"), 
				getRefsBySuffix("jsp").toString() + getRefsBySuffix("jspf"),
				getRefsBySuffix("disp"),
				"Menu TODO");
	}
	
//	String sqlCount = "SELECT COUNT(*) FROM fileusage WHERE fullFilename=?";
//	String sqlUpdate = "UPDATE fileusage SET imports=?, usesReflection=? ";
//	String sqlUpateOrInsert = "INSERT INTO fileusage (filename) VALUES (?)" +
//			"  ON DUPLICATE KEY UPDATE referencedInMenu = ?";
}
