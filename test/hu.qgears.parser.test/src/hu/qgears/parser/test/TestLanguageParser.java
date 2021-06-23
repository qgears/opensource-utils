package hu.qgears.parser.test;


import org.junit.Assert;
import org.junit.Test;

import hu.qgears.parser.LanguageParserText;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.TreeRenderer;
import hu.qgears.parser.impl.Parser;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.UtilLanguage;
import hu.qgears.parser.util.UtilFile;

public class TestLanguageParser {

	@Test
	public void run() throws Exception {
		String text = UtilFile.loadFileAsString(getClass().getResource(
				"xpath.txt"));
		ILanguage lang = LanguageParserText.getInstance()
			.parseLanguage(new TokenizerImplManager(), text, null);
		UtilLanguage.checkLanguage(lang);
		String txt = UtilFile.loadFileAsString(getClass().getResource(
				"testXpath.txt"));
		Parser p = new Parser(lang, txt, new ParserLogger(System.err));
		p.tokenize();
		TreeElem elem=p.parse(null);
		String tree=new TreeRenderer().render2(elem, "");
		Assert.assertEquals(UtilFile.loadFileAsString(getClass().getResource("testXpath-output.txt")), tree);
	}
}
