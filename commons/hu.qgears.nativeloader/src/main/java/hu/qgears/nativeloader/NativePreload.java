package hu.qgears.nativeloader;

import java.net.URL;

/**
 * Represents a native library that must be present on a preload path.
 */
public class NativePreload {
	private final String fileName;
	private final String resource;
	
	public NativePreload(final String fileName, final String resource) {
		if(fileName == null || resource == null) {
			throw new NullPointerException((fileName == null ? "filename"
					: "resource") + "must not be null");
		} else {
			this.fileName=fileName;
			this.resource=resource;
		}
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public boolean exists(INativeLoader nativeLoader) {
		URL res=nativeLoader.getClass().getResource(resource);
		return res != null;
	}
}
