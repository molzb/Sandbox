package bm.scanner;

import java.io.File;
import java.io.FileFilter;
import lombok.AllArgsConstructor;

/**
 *
 * @author Bernhard
 */
@AllArgsConstructor
public class FileUsageFilter implements FileFilter {
	private final String[] filesToScan;
	private boolean acceptDirs = true;

	@Override
	public boolean accept(File pathname) {
		for (String suffix : filesToScan) {
			if ( (pathname.isDirectory() && acceptDirs) || pathname.getName().endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}
}
