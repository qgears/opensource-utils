package hu.qgears.opengl.kmsgl;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.opengl.kms.KMSAccessor;
import hu.qgears.opengl.kms.KMSInstance;

public class KMSGLInstance {
	private static KMSGLInstance instance=new KMSGLInstance();

	public static KMSGLInstance getInstance() {
		return instance;
	}
	private KMSGLInstance()
	{
		UtilNativeLoader.loadNatives(new KMSGLAccessor());
	}

}
