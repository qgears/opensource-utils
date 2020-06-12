package hu.qgears.opengl.kms;

import hu.qgears.nativeloader.UtilNativeLoader;

public class KMSInstance {
	private static KMSInstance instance=new KMSInstance();

	public static KMSInstance getInstance() {
		return instance;
	}
	private KMSInstance()
	{
		UtilNativeLoader.loadNatives(new KMSAccessor());
	}
}
