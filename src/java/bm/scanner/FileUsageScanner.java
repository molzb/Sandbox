package bm.scanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Bernhard
 */
public class FileUsageScanner {
	private static final Logger logger = Logger.getLogger(FileUsageScanner.class.getName());
	private final String[] filesToScan = new String[] {
		".xhtml", ".html", ".htm", ".jspf", ".jsp", ".js",
		".xml", ".ini", ".properties",
		".java", ".pl", ".sql"/*, ".m" */
	};
	private final FileUsageFilter fileFilter;
	private final List<File> files = new ArrayList<File>();
	private final List<FileModel> fileModels = new ArrayList<FileModel>();

	public static void main(String[] args) {
		File rootDir = new File("C:/Users/Bernhard/Documents/NetBeansProjects/Sandbox");
		FileUsageScanner scanner = new FileUsageScanner(rootDir);
		System.out.println(
				scanner.extractFilenameFromLine("<script src=\"js/amcharts.js\"></script>", ".js"));
		System.out.println(
				scanner.extractFilenameFromLine("js/amcharts.js\"></script>", ".js"));
		scanner.printFileCount();
		scanner.showFiles(".jsp");
	}

	public FileUsageScanner(File rootDir) {
		if (!rootDir.exists()) {
			JOptionPane.showMessageDialog(new JFrame(), rootDir + " doesn't exist");
			System.exit(1);
		}
		fileFilter = new FileUsageFilter(filesToScan);
		if (rootDir.isDirectory()) {
			filterFiles(rootDir);
		} else {
			logger.severe("Can't scan " + rootDir.getAbsolutePath() + ", because it's not a directory");
		}
	}

	void printFileCount() {
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

	private void showFiles(String suffix) {
		for (File file : files) {
			if (file.getName().endsWith(suffix)) {
				try {
					FileModel fileModel = readFile(file);
					System.out.println(fileModel);
					fileModels.add(fileModel);
				} catch (IOException ioe) {
					logger.severe(ioe.getMessage());
				}
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
				if (lastSuffix.contains(suffix) && lastSuffixFound)
					continue;
				if (line.contains(suffix)) {
					fileModel.references.add(extractFilenameFromLine(line, suffix));
					lastSuffixFound = true;
				}
				lastSuffix = suffix;
			}
		}
		return fileModel;
	}
	
	private void makeReferences(List<File> files) {
		for (FileModel file : fileModels) {
			for (FileModel f : fileModels) {
//				if ()
			}
		}
	}

	private String extractFilenameFromLine(String line, String suffix) {
		char[] delimiters = new char[] {'\"', '\'', ',', ';'};
		line = line.trim();
		int posFilename = line.indexOf(suffix);
		int posOfSuffix = line.indexOf(suffix);

		while (posFilename > 0) {
			for (char c : delimiters) {
				if (line.charAt(posFilename) == c)
					return line.substring(posFilename+1, posOfSuffix + suffix.length());
			}
			posFilename--;
		}

		return line.substring(0, posOfSuffix + suffix.length());
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
	}

	@Getter @Setter File me;
	@Getter @Setter List<String> referencedBy = new ArrayList<String>();
	@Getter @Setter List<String> references = new ArrayList<String>();
	@Getter @Setter int lines;

	@Override
	public String toString() {
		return me.getAbsolutePath() + " (lines: " + lines + ")\n\tReferences=" + references + "\n\tReferencedBy=" + referencedBy;
	}
}