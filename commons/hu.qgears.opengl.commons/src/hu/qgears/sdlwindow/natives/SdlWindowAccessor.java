package hu.qgears.sdlwindow.natives;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class SdlWindowAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
