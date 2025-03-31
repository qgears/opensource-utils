package hu.qgears.opengl.glut;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class GlutAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
