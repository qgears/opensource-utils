package hu.qgears.parser.math;

import hu.qgears.commons.UtilFile;
import hu.qgears.parser.LanguageParserText;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.expression.ExpLang;
import hu.qgears.parser.impl.DefaultReceiver;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.UtilLanguage;
import hu.qgears.parser.tokenizer.recognizer.RecognizerDoubleNumber;
import hu.qgears.parser.tokenizer.recognizer.TokenTypeRegistry;

/**
 * Loads math expression language. You should not use this class directly, see
 * {@link EvaluateMathExpression} API instead.
 * 
 */
public class MathExpressionLanguage {
	private static MathExpressionLanguage instance;
	private ILanguage lang;

	private void init() throws Exception
	{
		String processed;
		String langDef=UtilFile.loadAsString(getClass().getResource("grammar.txt"));
		processed=ExpLang.processInlineExpressionLanguage(langDef, true);
		TokenTypeRegistry ttReg=new TokenTypeRegistry();
		ttReg.register("anyNumber", (type, id, config)->new RecognizerDoubleNumber(type).setAcceptWholeNumber(false).setAcceptPrevAndFollowingDot(false));
		TokenizerImplManager man=new TokenizerImplManager();
		man.addFact(ttReg);
		lang=LanguageParserText.getInstance().parseLanguage(man, processed, new ParserLogger(),
				new DefaultReceiver() {
					@Override
					public void treeUnfiltered(TreeElem root) {
					}
				});
		UtilLanguage.simplifyLanguage(lang);
	}
	
	public static void main(String[] args) {
		
	}
	public static MathExpressionLanguage getInstance() {
		if(instance==null)
		{
			synchronized (MathExpressionLanguage.class) {
				if(instance==null)
				{
					try {
						MathExpressionLanguage cp=new MathExpressionLanguage();
						cp.init();
						instance=cp;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return instance;
	}
	public ILanguage getLang() {
		return lang;
	}
}
