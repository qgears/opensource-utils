package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

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
	public int getGeneratedToken(TextSource src) {
		int at=src.getPosition();
		int nstart;
		if(startEscapeChar!=null)
		{
			nstart=RecognizerIdStart.getGeneratedToken(src.array, at, startEscapeChar);
		}else
		{
			nstart=RecognizerIdStart.getGeneratedToken(src.array, at);
		}
		if(nstart>0)
		{
			int ninside=RecognizerIdInside.recognize(src.array, at+nstart);
			return ninside+nstart;
		}
		return 0;
	}
}
