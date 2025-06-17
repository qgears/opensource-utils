package hu.qgears.xtextgrammar;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.recognizer.LetterAcceptorId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerIdInside;
import hu.qgears.parser.tokenizer.recognizer.RecognizerIdStart;

public class RecognizerXtextId extends RecognizerAbstract {
	private Character startEscapeChar;
	public RecognizerXtextId(ITokenType tokenType) {
		this(tokenType, null);
	}
	public RecognizerXtextId(ITokenType tokenType, Character startEscapeChar) {
		super(tokenType);
		this.startEscapeChar=startEscapeChar;
	}
	public static String unescape(String inDoc, String startEscapeChar)
	{
		if(inDoc.startsWith(startEscapeChar))
		{
			return inDoc.substring(startEscapeChar.length());
		}else
		{
			return inDoc;
		}
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}
	@Override
	public int getGeneratedToken(char[] array, int at) {
		int nstart;
		if(startEscapeChar!=null)
		{
			nstart=RecognizerIdStart.getGeneratedToken(array, at, startEscapeChar);
		}else
		{
			nstart=RecognizerIdStart.getGeneratedToken(array, at);
		}
		if(nstart>0)
		{
			int ninside=recognizeIdInside(array, at+nstart);
			return ninside+nstart;
		}
		return 0;
	}
	public static int recognizeIdInside(char[] arr, int at)
	{
		int ctr=0;
		for(;ctr<arr.length-at;++ctr)
		{
			char ch = arr[at+ctr];
			if(!Character.isJavaIdentifierPart(ch) || ch=='$')
			{
				return ctr;
			}
		}
		return ctr;
	}

	@Override
	public boolean tokenCanStartWith(char c) {
		if(startEscapeChar != null && c == startEscapeChar)
		{
			return true;
		}
		else
		{
			return Character.isJavaIdentifierStart(c);
		}
	}
}
