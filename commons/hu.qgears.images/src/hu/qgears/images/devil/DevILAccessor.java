package hu.qgears.images.devil;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class DevILAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
