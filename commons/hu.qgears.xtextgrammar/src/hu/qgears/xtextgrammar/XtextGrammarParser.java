package hu.qgears.xtextgrammar;

import hu.qgears.parser.IParser;
import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.LanguageHelper;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.tokenizer.IToken;

/**
 * Instantiate parser and execute syntactic parsing of XText files.
 */
public class XtextGrammarParser {
	private ElemBuffer buffer=new ElemBuffer();

	public ITreeElem parse(String source, IParserReceiver pr) throws Exception
	{
		IParser p = LanguageHelper.createParser(XTextParserLanguage.getInstance().getLang(), source,
				new ParserLogger());
		p.setBuffer(buffer);
		ITreeElem te = p.parse(pr);
		return te;
	}

	public boolean isFiltered(IToken iToken) {
		return XTextParserLanguage.getInstance().getLang().getTokenFilterDef().getToFilter().contains(iToken.getTokenType().getName());
	}
}
