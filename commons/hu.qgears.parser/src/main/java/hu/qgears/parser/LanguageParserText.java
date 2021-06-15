package hu.qgears.parser;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.Document;

import hu.qgears.parser.impl.Parser;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.LanguageParserAST;
import hu.qgears.parser.language.impl.LanguageParserXML;
import hu.qgears.parser.language.impl.UtilLanguage;
import hu.qgears.parser.util.UtilFile;
import hu.qgears.parser.util.UtilXml;

/**
 * Parse a language definition from a textual language definition file.
 * 
 * @author rizsi
 * 
 */
public class LanguageParserText {
	private static LanguageParserText instance = new LanguageParserText();

	public static LanguageParserText getInstance() {
		return instance;
	}

	private ILanguage languageLanguage;

	/**
	 * Get the language used to parse languages.
	 * @return
	 * @throws Exception
	 */
	public ILanguage getLanguageLanguage() throws Exception {
		checkInit();
		return languageLanguage;
	}

	public ILanguage parseLanguage(TokenizerImplManager tokenManager,
			String text, ParserLogger parserLogger) throws Exception {
		return parseLanguage(tokenManager, text, parserLogger, null);
	}
	public ILanguage parseLanguage(TokenizerImplManager tokenManager,
			String text, ParserLogger parserLogger, IParserReceiver receiver) throws Exception {
		if(parserLogger==null)
		{
			parserLogger=new ParserLogger(System.err);
		}
		checkInit();
		Parser p = new Parser(languageLanguage, text, parserLogger);
		p.tokenize();
		TreeElem root = p.parse(receiver);
		ILanguage ret=LanguageParserAST.buildLanguageFromAST(tokenManager, root);
		return ret;
	}

	private synchronized void checkInit() throws Exception {
		if (languageLanguage == null) {
			Class<?> parentClass=UtilLanguage.class;
			Document doc = UtilXml.loadDocument(parentClass
					.getResource("languageLanguage_v01.xml"));
			ILanguage langLang = LanguageParserXML.parseLanguage(doc);

			String l = getLanguage(parentClass
					.getResource("languageLanguage.txt"),
					parentClass
					.getResource("languageLanguageExpressions.txt"));
			ParserLogger parserLogger=new ParserLogger();
			Parser q = new Parser(langLang, l, parserLogger);
			q.tokenize();
			TreeElem qRoot = q.parse(null);
			languageLanguage = LanguageParserAST.buildLanguageFromAST(
					new TokenizerImplManager(), qRoot);
		}
	}
	private String getLanguage(URL lang, URL expr) throws IOException
	{
		String l = UtilFile.loadFileAsString(lang);
		String exp = UtilFile.loadFileAsString(expr);

		l = LanguageHelper.addExpressionLanguage(l, exp);
		return l;
	}
}
