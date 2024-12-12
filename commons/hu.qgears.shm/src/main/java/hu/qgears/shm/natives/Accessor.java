package hu.qgears.shm.natives;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;

/**
 * Accessor to native libraries
 * @author rizsi
 *
 */
public class Accessor  extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
