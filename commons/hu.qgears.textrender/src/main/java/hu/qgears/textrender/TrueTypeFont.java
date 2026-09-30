package hu.qgears.textrender;

public class TrueTypeFont {

	public String ttfFilePath;
	private double letterSpacing;
	private float fontSize;
	private long nativePtr = 0;
	
	public TrueTypeFont(String ttfFilePath, float fontSize) {
		this.ttfFilePath = ttfFilePath;
		this.fontSize = fontSize;
	}
	
	public void setLetterSpacing(double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}
	
	public void dispose() {
		nativeDispose();
	}

	private native void nativeDispose();
}
