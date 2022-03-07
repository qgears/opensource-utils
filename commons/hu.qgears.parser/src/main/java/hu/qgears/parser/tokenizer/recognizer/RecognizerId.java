package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.TokenType;

public class RecognizerId extends RecognizerConcat {

	public RecognizerId(ITokenType tokenType) {
		this(tokenType, null);
	}
	public RecognizerId(ITokenType tokenType, Character startEscapeChar) {
		super(tokenType);
		addSubToken(new RecognizerIdStart(new TokenType("dummy"), startEscapeChar), true);
		addSubToken(new RecognizerIdInside(new TokenType("dummy")), false);
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

}
