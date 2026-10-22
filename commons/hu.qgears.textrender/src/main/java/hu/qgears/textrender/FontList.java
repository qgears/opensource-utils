package hu.qgears.textrender;

import java.util.List;

public class FontList {

	
	/**
	 * Accessed by native code
	 * 
	 */
	public TrueTypeFont[] fonts;

	public FontList(TrueTypeFont[] fonts) {
		super();
		this.fonts = fonts;
	}
	
	public static FontList from(TrueTypeFont... fonts) {
		if (fonts == null) {
			throw new IllegalArgumentException("'fonts' must not be null");
		}
		return new FontList(fonts);
	}
	
	public static FontList from(List<TrueTypeFont> fonts) {
		if (fonts == null || fonts.isEmpty()) {
			throw new IllegalArgumentException("'fonts' must not be null or empty");
		}
		return new FontList(fonts.toArray(new TrueTypeFont[fonts.size()]));
	}
	
}
