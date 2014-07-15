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
