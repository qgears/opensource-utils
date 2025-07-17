package hu.qgears.opengl.osmesa;

import hu.qgears.nativeloader.UtilNativeLoader;
import lwjgl.standalone.BaseAccessor;
import lwjgl.standalone.BaseAccessor.ELwjglOpenGlImpl;

public class OSMesaInstance {
	private static OSMesaInstance instance=new OSMesaInstance();

	public static OSMesaInstance getInstance() {
		return instance;
	}
	private OSMesaInstance()
	{
		UtilNativeLoader.loadNatives(new OsMesaAccessor());
	}
	
	public void initLwjgl() {
		
	}
	public void bindLwjglNatives() {
		BaseAccessor.initLwjglNatives(ELwjglOpenGlImpl.OSMESA);
	}
}
