package hu.qgears.images.libpng;

import hu.qgears.nativeloader.UtilNativeLoader;


public class NativeLibPngManager {
	private static NativeLibPngManager instance;
	synchronized public static NativeLibPngManager getInstance() throws Exception {
		if(instance==null)
		{
			instance=new NativeLibPngManager();
		}
		return instance;
	}
	private NativeLibPngManager() throws Exception {
		UtilNativeLoader.loadNatives(new NativeLibPngAccessor());
	}
}
