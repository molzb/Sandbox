package bm.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Bernhard
 */
public class FileModel {

	FileModel(File me) {
		this.me = me;
		webFilename = me.getAbsolutePath().replace(FileUsageScanner.contextRealPath, FileUsageScanner.context);
		webFilename = webFilename.replace('\\', '/').replace("//", "/");
	}

	@Getter
	@Setter
	private File me;
	@Getter
	@Setter
	private String webFilename;
	@Getter
	@Setter
	private List<String> referencedBy = new ArrayList<String>();
	@Getter
	@Setter
	private List<String> references = new ArrayList<String>();
	@Getter
	@Setter
	private int lines;
	@Getter
	@Setter
	private Set<String> imports = new HashSet<String>();

	private List<String> getRefsBySuffix(String suffix) {
		List<String> l = new ArrayList<String>();
		for (String ref : referencedBy) {
			if (ref.endsWith(suffix)) {
				l.add(ref);
			}
		}
		return l;
	}
	
	@Override
	public String toString() {
		return webFilename + " (lines: " + lines + ")\n\tReferences=" + references + "\n\tReferencedBy=" + referencedBy;
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
}
