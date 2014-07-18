package bm.scanner;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Bernhard
 */
public class JarFileModel extends FileModel {

	public JarFileModel(File me) {
		super(me);
	}
	
	@Getter @Setter 
	private Set<String> packages = new HashSet<String>();
	
	@Override
	public String toString() {
		return super.getMe().getName() + " (Packages:\n\t" + packages + "\n\tReferencedBy=" + getReferencedBy();
	}
}
