package hu.qgears.parser;

import java.io.IOException;

public interface ILanguageProvider {
	public static final String service = ILanguageProvider.class.getName();
	public static final String LANG_ID = "lang_id";

	String getLangId();

	String getLanguageDefintion() throws IOException;
}
