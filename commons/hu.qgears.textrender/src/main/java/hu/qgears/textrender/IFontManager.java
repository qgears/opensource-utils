package hu.qgears.textrender;

import hu.qgears.images.text.TextParameters;

public interface IFontManager {

	TrueTypeFont getFont(TextParameters t);

	void dispose();
}
