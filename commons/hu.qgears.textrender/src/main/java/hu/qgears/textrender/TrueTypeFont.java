package hu.qgears.textrender;

import hu.qgears.textrender.libschrift.LibschriftAccessor;

public class TrueTypeFont {

	public String ttfFilePath;
	private long nativePtr = 0;
	
	public TrueTypeFont(String ttfFilePath) {
		this.ttfFilePath = ttfFilePath;
		LibschriftAccessor.getInstance();
		nativeInit();
	}
	
	public synchronized void dispose() {
		if (nativePtr != 0) {
			nativeDispose();
			nativePtr = 0;
		}
	}

	private native void nativeDispose();
	private native void nativeInit();
}
