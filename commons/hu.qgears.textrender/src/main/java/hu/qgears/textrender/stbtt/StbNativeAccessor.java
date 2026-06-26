package hu.qgears.textrender.stbtt;

import java.io.File;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

public class StbNativeAccessor extends XmlNativeLoader {
	
	private static StbTrueTypeNative instance;
	
	private StbNativeAccessor() {}
	
	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}
	
	public static synchronized StbTrueTypeNative getInstance() {
		if (instance == null) {
			instance = new StbTrueTypeNative();
			UtilNativeLoader.loadNatives(new StbNativeAccessor());
		}
		return instance;
	}
}