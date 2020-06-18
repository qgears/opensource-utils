package hu.qgears.nativeloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of native libraries to load.
 */
public class NativesToLoad {
	private List<NativePreload> preloads;
	private List<NativeBinary> binaries;
	private List<SourceFile> sources;
	public NativesToLoad(List<NativePreload> preloads, List<NativeBinary> binaries, List<SourceFile> sources) {
		super();
		this.preloads=preloads;
		this.binaries = binaries;
		this.sources = sources;
	}
	public NativesToLoad() {
		super();
		this.preloads = new ArrayList<NativePreload>();
		this.binaries = new ArrayList<NativeBinary>();
		this.sources = new ArrayList<SourceFile>();
	}
	public List<NativeBinary> getBinaries() {
		return binaries;
	}
	public List<SourceFile> getSources() {
		return sources;
	}
	public List<NativePreload> getPreloads() {
		return preloads;
	}
}
