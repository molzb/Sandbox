package bm.scanner;

import java.io.File;
import lombok.Getter;
import lombok.Setter;

/**
 * FileModel for Java, JSP and JSP files
 * @author Bernhard
 */
public class JavaFileModel extends FileModel {

	@Getter
	@Setter
	private boolean usesReflection = false;

	public JavaFileModel(File me) {
		super(me);
	}
}
