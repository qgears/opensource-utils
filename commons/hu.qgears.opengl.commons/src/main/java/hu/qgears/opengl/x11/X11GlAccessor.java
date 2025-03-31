package hu.qgears.opengl.x11;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class X11GlAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
