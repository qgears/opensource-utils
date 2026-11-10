package hu.qgears.textrender;

import java.util.List;

import hu.qgears.images.text.ETextDecoration;
import hu.qgears.images.text.TextParameters;

public class FontSettings {

	
	/**
	 * Accessed by native code
	 * 
	 */
	public TrueTypeFont[] fonts;

	private double letterSpacing;
	private float fontSize;
	private boolean isUnderline;
	
	public FontSettings(float fontSize, TrueTypeFont[] fonts) {
		super();
		if (fontSize  <= 0) {
			throw new IllegalArgumentException("fontSize must be positive");
		}
		this.fontSize = fontSize;
		this.fonts = fonts;
	}

	public double getLetterSpacing() {
		return letterSpacing;
	}

	public void setLetterSpacing(double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isUnderline() {
		return isUnderline;
	}

	public void setUnderline(boolean isUnderline) {
		this.isUnderline = isUnderline;
	}

	
	public static FontSettings from(TextParameters t, TrueTypeFont... fonts) {
		if (fonts == null) {
			throw new IllegalArgumentException("'fonts' must not be null");
		}
		FontSettings f = new FontSettings(t.fontSize, fonts);
		f.setUnderline(ETextDecoration.underline.equals(t.textDecoration));
		f.setLetterSpacing(t.letterSpacing);
		return f;
	}
	
	public static FontSettings from(TextParameters t, List<TrueTypeFont> fonts) {
		if (fonts == null || fonts.isEmpty()) {
			throw new IllegalArgumentException("'fonts' must not be null or empty");
		}
		return from (t, fonts.toArray(new TrueTypeFont[fonts.size()]));
	}
	
	
	
}
