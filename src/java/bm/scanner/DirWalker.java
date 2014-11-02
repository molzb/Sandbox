package bm.scanner;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Bernhard
 */
public class DirWalker {

	private final String rootDir = "C:/Users/Bernhard/Documents/NetBeansProjects/";
	private String htmlList;

	public static void main(String[] args) {
		DirWalker dirWalker = new DirWalker();
		System.out.println(dirWalker.getHtmlList());
	}

	private final String[] tabs = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t",
		"\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
		"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
	};
	
	public DirWalker() {
	}
	
	private enum ICONS { clazz, dir, jar, java, sql, txt, xml };

	public final void walk(File f, StringBuilder sb, int indent) {
		String fname = f.getName(), tab = tabs[indent];
		if (f.isDirectory()) {
			sb.append(tab).append("<li class='dir'>").append(fname).append("\n");
			File[] files = f.listFiles(new MyFileFilter(indent));
			if (files != null && files.length > 0) {
				sb.append(tab).append("<ul>\n");
				for (File file : files) {
					walk(file, sb, indent + 1);
				}
				sb.append(tab).append("</ul>\n");
			} else {
				sb.append("</li>\n");
			}
		} else { // isFile
			String sfx = getSuffix(f);
			switch (getSuffix(f)) {
				case "png":
				case "gif":
				case "jpg": sfx = "img"; break;
				case "class": sfx = "clazz"; break;
				case "htm":
				case "xhtml": sfx = "html"; break;
				case "properties":
				case "txt": sfx = "txt"; break;
				default: throw new IllegalArgumentException("No case for " + getSuffix(f));
			}
			sb.append(tab).append("<li class='file " + sfx + "'>").append(fname).append("</li>\n");
		}
	}
	
	private String getSuffix(File f) {
		int i = f.getName().lastIndexOf('.');
		return i > 0 ? f.getName().substring(i + 1) : "";
	}

	public String getHtmlList() {
		File root = new File(rootDir);
		StringBuilder sb = new StringBuilder();
		if (root.exists() && root.isDirectory()) {
			walk(root, sb, 1);
		}
		htmlList = "<ul>\n" + sb + "</ul>";

		return htmlList;
	}
}

class MyFileFilter implements FileFilter {

	List<String> suffices = new ArrayList<>();
	private int level = 1;
	
	MyFileFilter(int level) {
		this.level = level;
		suffices.addAll(Arrays.asList(FileUsageScanner.FILES_TO_DISPLAY));
		suffices.addAll(Arrays.asList(FileUsageScanner.FILES_TO_SCAN));
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return level != 2 || (level == 2 && (f.getName().equals("src") || f.getName().equals("web")));
		}
		
		for (String suffix : suffices) {
			if (f.getName().toLowerCase().endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}
}
