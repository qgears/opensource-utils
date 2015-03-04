package hu.qgears.nativeloader;

import java.net.URL;

/**
 * Binary shared library to be loaded by the program.
 * 
 * Stores all information that is required to load the binary.
 * 
 * @author rizsi
 *
 */
public class NativeBinary {
	private String libPath;
	private String installPath;
	public NativeBinary(String libPath, String installPath) {
		super();
		this.libPath = libPath;
		this.installPath = installPath;
	}
	/**
	 * Get the path to the binary relative to the loader class.
	 * @return
	 */
	public String getLibPath() {
		return libPath;
	}
	public String getInstallPath() {
		return installPath;
	}
	
	/**
	 * Get the simple file name of the shared object.
	 * @return
	 */
	public String getFileName() {
		String fileName=libPath;
		int idx = fileName.lastIndexOf("/");
		if (idx >= 0) {
			fileName = fileName.substring(idx + 1);
		}
		return fileName;
	}
	/**
	 * Get the URL that can be used to read the binary shared object data.
	 * @param clazz
	 * @return
	 */
	public URL getUrl(Class<?> clazz) {
		String fixedLibPath = libPath.replaceAll("/", "_");
		URL ret=clazz.getResource(fixedLibPath);
		if(ret==null)
		{
			ret=clazz.getResource(libPath);
		}
		return ret;
	}
}
