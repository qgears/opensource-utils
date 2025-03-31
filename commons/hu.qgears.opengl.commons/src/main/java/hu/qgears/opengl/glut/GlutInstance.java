package hu.qgears.opengl.glut;

import hu.qgears.nativeloader.UtilNativeLoader;

public class GlutInstance {
	private static GlutInstance instance=new GlutInstance();

	public static GlutInstance getInstance() {
		return instance;
	}
	private GlutInstance()
	{
		UtilNativeLoader.loadNatives(new GlutAccessor());
	}
}
