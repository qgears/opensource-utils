package hu.qgears.textrender;

import hu.qgears.images.text.TextParameters;

public class DefaultFontManger implements IFontManager {

	private static TrueTypeFont LIB_SANS;

	private static synchronized TrueTypeFont getLibSans() {
		if (LIB_SANS == null) {
			LIB_SANS = new TrueTypeFont("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf");
		}
		return LIB_SANS;
	}

	@Override
	public FontSettings getFont(TextParameters t) {
		return FontSettings.from(t, getLibSans());
	}

	@Override
	public void dispose() {
		synchronized (DefaultFontManger.class) {
			if (LIB_SANS != null) {
				LIB_SANS.dispose();
				LIB_SANS = null;
			}
		}
	}

}
