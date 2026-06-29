package hu.qgears.textrender.libschrift;

import java.io.File;

import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

public class LibschriftAccessor extends XmlNativeLoader {
	private static LibschriftNative instance;

	private LibschriftAccessor() {
	}

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

	public static synchronized LibschriftNative getInstance() {
		if (instance == null) {
			instance = new LibschriftNative();
			UtilNativeLoader.loadNatives(new LibschriftAccessor());
		}
		return instance;
	}
}
