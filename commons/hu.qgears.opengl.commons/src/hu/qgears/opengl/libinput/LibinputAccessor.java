package hu.qgears.opengl.libinput;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class LibinputAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
