package hu.qgears.textrender;

import hu.qgears.images.text.TextParameters;

public interface IFontManager {

	FontList getFont(TextParameters t);

	void dispose();
}
