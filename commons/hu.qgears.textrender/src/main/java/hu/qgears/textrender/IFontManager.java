package hu.qgears.textrender;

import hu.qgears.images.text.TextParameters;

public interface IFontManager {

	FontSettings getFont(TextParameters t);

	void dispose();
}
