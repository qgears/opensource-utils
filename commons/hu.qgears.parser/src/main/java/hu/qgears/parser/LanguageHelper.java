package hu.qgears.parser;

import java.io.IOException;

import hu.qgears.parser.expression.ExpLang;
import hu.qgears.parser.impl.Parser;
import hu.qgears.parser.language.ILanguage;



/**
 * Helper methods for the generic parser.
 * 
 * @author rizsi
 * 
 */
public class LanguageHelper {
	public static String addExpressionLanguage(String language,
			String expressions) throws IOException {
		ExpLang el = ExpLang.parse(expressions);
		String expLang = el.renderExpLang();
		String lang = language.replaceAll(el.insertHere, expLang);
		return lang;
	}

	/**
	 * Create a parser instance for a language, an input text and a parse error
	 * logger. The returned parser is for single use! 
	 * @param lang
	 * @param text
	 * @param logger
	 * @return
	 * @throws Exception
	 */
	public static IParser createParser(ILanguage lang,
		String text,
		ParserLogger logger) throws Exception {
		return new Parser(lang, text, logger);
	}


	public static void print(ITreeElem tree) {
		System.out.println(new TreeRenderer().render2(tree, ""));
	}
}
