package hu.qgears.xtextgrammar;

import hu.qgears.commons.UtilFile;
import hu.qgears.parser.LanguageParserText;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.expression.ExpLang;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.UtilLanguage;
import hu.qgears.parser.tokenizer.recognizer.RecognizerDoubleNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.parser.tokenizer.recognizer.TokenTypeRegistry;

/**
 * Instantiate parser and execute syntactic parsing of XText grammar files.
 */
public class XTextParserLanguage {
	private static XTextParserLanguage instance;
	private ILanguage lang;

	private void init() throws Exception
	{
		String langDef=UtilFile.loadAsString(getClass().getResource("language.txt"));
		String processed=ExpLang.processInlineExpressionLanguage(langDef, true);
		TokenTypeRegistry ttReg=new TokenTypeRegistry();
		ttReg.register("anyNumber", (type, id, config)->new RecognizerDoubleNumber(type));
		ttReg.register("stringConst2", (type, id, config)->new RecognizerString(type, '\''));
		TokenizerImplManager man=new TokenizerImplManager();
		man.addFact(ttReg);
		
		// TODO remove
		// UtilFile.saveAsFile(new File("/tmp/xtextlang.txt"), processed);
		// System.out.println("File saved to temp folder. "+"/tmp/xtextlang.txt");
		
		lang=LanguageParserText.getInstance().parseLanguage(man, processed, new ParserLogger());
		UtilLanguage.simplifyLanguage(lang);
	}
	public static XTextParserLanguage getInstance() {
		if(instance==null)
		{
			synchronized (XTextParserLanguage.class) {
				if(instance==null)
				{
					try {
						XTextParserLanguage cp=new XTextParserLanguage();
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
