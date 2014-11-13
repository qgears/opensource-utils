package hu.qgears.nativeloader;

import java.io.File;

/**
 * Caller of native loader must implement this interface.
 * 
 * Trick - JVM implementation only allows access of loaded native libs from
 * classloader that invokes the runtime.loadLibrary(). That's why the caller
 * must implement INativeLoader
 * 
 * @author rizsi
 * 
 */
public interface INativeLoader {
	/**
	 * Load the native library in the file given as parameter. Implementation
	 * must be: Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	 * 
	 * @param g
	 * @throws Throwable
	 */
	void load(File nativeLibFile) throws Throwable;

	/**
	 * Return the paths to the libraries which are to be loaded on a specific
	 * arch and os.
	 * 
	 * The libraries are loaded in order they are returned, so that they should
	 * be topologically shorted with regards to dependencies, or errors may
	 * arise.
	 * 
	 * @param arch
	 *            the arch
	 * @param os
	 *            the os
	 * @return the natives
	 * @throws NativeLoadException
	 */
	NativesToLoad getNatives(String arch, String os) throws NativeLoadException;
}
