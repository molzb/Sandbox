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
	private boolean usesReflection;
	@Getter @Setter
	private String clazzName;

	public JavaFileModel(File file) {
		super(file);
	}
}
