package hu.qgears.nativeloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of native libraries to load.
 * @author rizsi
 *
 */
public class NativesToLoad {
	private List<NativeBinary> binaries;
	
	public NativesToLoad(List<NativeBinary> binaries) {
		super();
		this.binaries = binaries;
	}
	public NativesToLoad() {
		super();
		this.binaries = new ArrayList<NativeBinary>();
	}
	public List<NativeBinary> getBinaries() {
		return binaries;
	}
	
}
