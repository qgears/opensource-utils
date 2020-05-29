package hu.qgears.opengl.osmesa;

import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;


public class OsMesaAccessor extends XmlNativeLoader {

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
