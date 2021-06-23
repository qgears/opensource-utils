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
import hu.qgears.parser.impl.Parser;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.util.UtilFile;



public class TestExpression {
	@Test
	public void testExpression1() throws Exception {
			String lang = UtilFile.loadFileAsString(getClass().getResource(
					"lang.txt"));
			String expLangFile = UtilFile.loadFileAsString(getClass()
					.getResource("expressionLanguage.txt"));
			lang = LanguageHelper.addExpressionLanguage(lang, expLangFile);
			ILanguage iLang=LanguageParserText.getInstance().parseLanguage(new TokenizerImplManager(),
					lang, null);
			{
			IParser p=
				LanguageHelper.createParser(iLang,
						"a+b+c+d+e+f+g",
						new ParserLogger(System.err) );
			ITreeElem te =p.parse(null);
			}
			IParser p=
				LanguageHelper.createParser(iLang,
						"fn(a+\"alma\"+b+c+d+e+f,b,c)",
						new ParserLogger(System.err) );
			ITreeElem te =p.parse(null);
			String s=new TreeRenderer().render2(te, "");
			Assert.assertEquals(UtilFile.loadFileAsString(getClass().getResource("TestExpression.txt")), s);
			Assert.assertEquals("doc", te.getTypeName());
			Assert.assertEquals(1, te.getSubs().size());
			ITreeElem fnCall = te.getSubs().get(0);
			Assert.assertEquals("fnCall", fnCall.getTypeName());
			Assert.assertEquals(4, fnCall.getSubs().size());
			ITreeElem id = fnCall.getSubs().get(0);
			Assert.assertEquals("tId", id.getTypeName());
			Assert.assertEquals("fn", id.getString());
	}
}
