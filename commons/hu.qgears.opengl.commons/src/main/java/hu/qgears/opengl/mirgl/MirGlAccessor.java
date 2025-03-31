package hu.qgears.opengl.mirgl;

import hu.qgears.nativeloader.XmlNativeLoader2;

import java.io.File;


public class MirGlAccessor extends XmlNativeLoader2 {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
