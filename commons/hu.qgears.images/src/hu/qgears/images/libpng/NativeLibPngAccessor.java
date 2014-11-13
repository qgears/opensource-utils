package hu.qgears.images.libpng;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class NativeLibPngAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
