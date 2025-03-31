package hu.qgears.opengl.osmesa;

import hu.qgears.nativeloader.UtilNativeLoader;

public class OSMesaInstance {
	private static OSMesaInstance instance=new OSMesaInstance();

	public static OSMesaInstance getInstance() {
		return instance;
	}
	private OSMesaInstance()
	{
		UtilNativeLoader.loadNatives(new OsMesaAccessor());
	}
}
