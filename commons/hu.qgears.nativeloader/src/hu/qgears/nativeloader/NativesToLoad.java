package hu.qgears.nativeloader;

import java.util.Collections;
import java.util.List;

/**
 * Set of native libraries to load.
 * @author rizsi
 *
 */
public class NativesToLoad {
	private List<NativeBinary> binaries;
	
	public NativesToLoad(List<NativeBinary> binaries) {
		this.binaries = Collections.unmodifiableList(binaries);
	}
	
	public List<NativeBinary> getBinaries() {
		return Collections.unmodifiableList(binaries);
	}
	
}
