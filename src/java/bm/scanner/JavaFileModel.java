package bm.scanner;

import java.io.File;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Bernhard
 */
class JavaFileModel extends FileModel {
	@Getter @Setter
	private String clazzName; // fully qualified name, e.g. bm.scanner.JavaFileModel

	public JavaFileModel(File file) {
		super(file);
	}
}
