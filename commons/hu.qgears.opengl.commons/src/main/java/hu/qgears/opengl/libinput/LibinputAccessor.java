package hu.qgears.opengl.libinput;

import java.io.File;

import hu.qgears.nativeloader.XmlNativeLoader3;


public class LibinputAccessor extends XmlNativeLoader3 {

	private static LibinputAccessor instance=new LibinputAccessor();
	private LibinputAccessor() {
	}
	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}
	public static LibinputAccessor getInstance() {
		instance.load();
		return instance;
	}
}
