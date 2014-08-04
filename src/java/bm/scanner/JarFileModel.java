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
public class JarFileModel extends FileModel {

	public JarFileModel(File me) {
		super(me);
	}
	
	@Getter @Setter 
	private Set<String> packages = new HashSet<>();
	@Getter @Setter
	private List<String> unidentifiableExternalReferences = new ArrayList<>();
	
	@Override
	public String toString() {
		return super.getMe().getName() +
				"\n\tReferencedBy=" + getUsedBy() +
				"\n\tReferences=" + getUses() +
				"\n\tUnidentifiableReferences=" + getUnidentifiableExternalReferences() +
				"\n\tPackages=" + getPackages();
	}
}
