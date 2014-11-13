package hu.qgears.nativeloader;

import java.util.List;

public class NativesToLoad {
	private List<NativeBinary> binaries;
	private List<SourceFile> sources;
	public NativesToLoad(List<NativeBinary> binaries, List<SourceFile> sources) {
		super();
		this.binaries = binaries;
		this.sources = sources;
	}
	public List<NativeBinary> getBinaries() {
		return binaries;
	}
	public List<SourceFile> getSources() {
		return sources;
	}
	
}
