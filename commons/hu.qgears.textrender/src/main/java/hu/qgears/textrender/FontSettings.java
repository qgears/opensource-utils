package hu.qgears.textrender;

import java.util.Collections;
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
		if (fontSize <= 0) {
			throw new IllegalArgumentException("fontSize must be positive");
		}
		if (fonts == null) {
			throw new IllegalArgumentException("'fonts' must not be null");
		}
		if (fonts.length == 0) {
			throw new IllegalArgumentException("'fonts' must not be empty");
		}
		for (int i = 0; i < fonts.length; i++) {
			if (fonts[i] == null) {
				throw new IllegalArgumentException("'fonts[" + i + "]' must not be null");
			}
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

	public boolean isUnderline() {
		return isUnderline;
	}

	public void setUnderline(boolean isUnderline) {
		this.isUnderline = isUnderline;
	}

	public static FontSettings from(TextParameters t, TrueTypeFont font) {
		if (font == null) {
			throw new IllegalArgumentException("'font' must not be null");
		}
		return from(t,Collections.singletonList(font));
	}

	public static FontSettings from(TextParameters textParameters, List<TrueTypeFont> fonts) {
		if (fonts == null || fonts.isEmpty()) {
			throw new IllegalArgumentException("'fonts' must not be null or empty");
		}
		if (textParameters == null) {
			throw new IllegalArgumentException("'textParameters' must not be null");
		}
		FontSettings f = new FontSettings(textParameters.fontSize, fonts.toArray(new TrueTypeFont[fonts.size()]));
		f.setUnderline(ETextDecoration.underline.equals(textParameters.textDecoration));
		f.setLetterSpacing(textParameters.letterSpacing);
		return f;
	}

}
