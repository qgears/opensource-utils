package hu.qgears.parser.test.expression;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.parser.IParser;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.LanguageHelper;
import hu.qgears.parser.LanguageParserText;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.TreeRenderer;
import hu.qgears.parser.expression.ExpLang;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.util.UtilFile;

public class TestExpression2 {
	@Test
	public void testExpression2() throws Exception {
		String lang=UtilFile.loadFileAsString(getClass().getResource("lang_with_expression_language2.txt"));
		String language=ExpLang.processInlineExpressionLanguage(lang, false);
		ILanguage iLang = LanguageParserText.getInstance().parseLanguage(
				new TokenizerImplManager(), language, null);
		IParser p = LanguageHelper.createParser(iLang, "a+b+c+d+e+f+g+h+i+j",
				new ParserLogger(System.err));
		ITreeElem te = p.parse(null);
		String s=new TreeRenderer().render2(te, "");
		Assert.assertEquals(UtilFile.loadFileAsString(getClass().getResource("TestExpression2.txt")), s);
	}
}
