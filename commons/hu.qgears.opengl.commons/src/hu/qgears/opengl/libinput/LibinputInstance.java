package hu.qgears.opengl.libinput;

import hu.qgears.nativeloader.UtilNativeLoader;

public class LibinputInstance {
	private static LibinputInstance instance=new LibinputInstance();

	public static LibinputInstance getInstance() {
		return instance;
	}
	private LibinputInstance()
	{
		UtilNativeLoader.loadNatives(new LibinputAccessor());
	}
}
