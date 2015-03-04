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
	private List<SourceFile> sources;
	public NativesToLoad(List<NativeBinary> binaries, List<SourceFile> sources) {
		super();
		this.binaries = binaries;
		this.sources = sources;
	}
	public NativesToLoad() {
		super();
		this.binaries = new ArrayList<NativeBinary>();
		this.sources = new ArrayList<SourceFile>();
	}
	public List<NativeBinary> getBinaries() {
		return binaries;
	}
	public List<SourceFile> getSources() {
		return sources;
	}
	
}
