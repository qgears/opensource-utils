package hu.qgears.textrender;

public class TrueTypeFont {

	public String ttfFilePath;
	private double letterSpacing;
	private float fontSize;
	private long nativePtr = 0;
	
	public TrueTypeFont(String ttfFilePath, float fontSize) {
		this.ttfFilePath = ttfFilePath;
		this.fontSize = fontSize;
		nativeInit();
	}
	
	public void setLetterSpacing(double letterSpacing) {
		this.letterSpacing = letterSpacing;
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
