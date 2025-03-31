package hu.qgears.opengl.kms;

import java.io.File;

import hu.qgears.nativeloader.XmlNativeLoader3;

public class KMSInstance extends XmlNativeLoader3 {
	private static KMSInstance instance=new KMSInstance();

	public static KMSInstance getInstance() {
		return instance;
	}
	private KMSInstance()
	{
		load();
	}
	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}
}
