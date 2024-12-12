package hu.qgears.images.tiff;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class NativeTiffLoaderAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}
}
