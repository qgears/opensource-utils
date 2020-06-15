package hu.qgears.opengl.kmsgl;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class KMSGLAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
