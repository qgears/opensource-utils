package hu.qgears.textrender;

public class TrueTypeFont {

	public String fontFamily;
	public float fontSize;
	public double letterSpacing;
	
	public boolean bold;
	public boolean italic;
	public boolean underline;

	public TrueTypeFont(String fontFamily, float fontSize) {
		super();
		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
	}
	
}
