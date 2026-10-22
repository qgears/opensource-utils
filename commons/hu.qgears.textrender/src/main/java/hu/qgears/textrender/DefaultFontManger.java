package hu.qgears.textrender;

import hu.qgears.images.text.TextParameters;

public class DefaultFontManger implements IFontManager {

	private static final TrueTypeFont LIB_SANS = new TrueTypeFont("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",12f);

	@Override
	public FontList getFont(TextParameters t) {
		
		return FontList.from(LIB_SANS);
	}

	@Override
	public void dispose() {
		LIB_SANS.dispose();
	}

}
