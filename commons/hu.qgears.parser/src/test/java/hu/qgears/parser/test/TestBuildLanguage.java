package hu.qgears.parser.test;

import org.junit.Test;
import org.w3c.dom.Document;

import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.impl.DefaultReceiver;
import hu.qgears.parser.impl.Parser;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.LanguageParserXML;
import hu.qgears.parser.language.impl.UtilLanguage;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.impl.TextSource;
import hu.qgears.parser.tokenizer.impl.TokenFilter;
import hu.qgears.parser.tokenizer.impl.Tokenizer;
import hu.qgears.parser.util.UtilFile;
import hu.qgears.parser.util.UtilXml;

/**
 * Parse a language file using the bootstrapping language language.
 */
public class TestBuildLanguage {
	@Test
	public void testBuildLanguage() throws Exception {
		Document doc = UtilXml.loadDocument(LanguageParserXML.class
				.getResource("languageLanguage_v01.xml"));
		ILanguage lang = LanguageParserXML.parseLanguage(doc);
		Tokenizer tok = new Tokenizer(lang.getTokenizerDef());
		TextSource ts = new TextSource(UtilFile.loadFileAsString(getClass()
				.getResource("in1_1.txt")));
		TokenArray toks=new TokenArray(ts, lang);
		tok.tokenize(toks, ts, new DefaultReceiver());
		toks = new TokenFilter(lang.getTokenFilterDef()).filter(toks);
		UtilLanguage.checkLanguage(lang);
		new Parser(lang, "", new ParserLogger(System.out));
	}
}
