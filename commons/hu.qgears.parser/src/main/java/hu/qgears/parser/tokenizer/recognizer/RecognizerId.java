package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;

public class RecognizerId extends RecognizerAbstract {
	Character startEscapeChar;
	public RecognizerId(ITokenType tokenType) {
		this(tokenType, null);
	}
	public RecognizerId(ITokenType tokenType, Character startEscapeChar) {
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
			int ninside=RecognizerIdInside.recognize(array, at+nstart);
			return ninside+nstart;
		}
		return 0;
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
