package hu.qgears.textrender;

import hu.qgears.images.text.TextParameters;

public class DefaultFontManger implements IFontManager {

	private static final TrueTypeFont LIB_SANS = new TrueTypeFont("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf");

	@Override
	public FontSettings getFont(TextParameters t) {
		return FontSettings.from(t, LIB_SANS);
	}

	@Override
	public void dispose() {
		LIB_SANS.dispose();
	}

}
