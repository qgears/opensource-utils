package hu.qgears.nativeloader;

import java.util.Collections;
import java.util.List;

/**
 * Set of native libraries to load.
 */
public class NativesToLoad {
	private List<NativePreload> preloads;
	private List<NativeBinary> binaries;
	
	public NativesToLoad(List<NativePreload> preloads, List<NativeBinary> binaries) {
		super();
		this.preloads = preloads;
		this.binaries = binaries;
	}

	public NativesToLoad(List<NativeBinary> binaries) {
		this.binaries = binaries;
	}
	
	public List<NativeBinary> getBinaries() {
		return Collections.unmodifiableList(binaries);
	}

	public List<NativePreload> getPreloads() {
		return Collections.unmodifiableList(preloads);
	}
}
