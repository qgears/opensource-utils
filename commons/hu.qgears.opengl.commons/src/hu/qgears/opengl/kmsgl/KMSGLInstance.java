package hu.qgears.opengl.kmsgl;

import java.io.File;

import hu.qgears.nativeloader.XmlNativeLoader3;

public class KMSGLInstance extends XmlNativeLoader3 {
	private static KMSGLInstance instance=new KMSGLInstance();

	public static KMSGLInstance getInstance() {
		return instance;
	}
	private KMSGLInstance()
	{
		load();
	}

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

}
