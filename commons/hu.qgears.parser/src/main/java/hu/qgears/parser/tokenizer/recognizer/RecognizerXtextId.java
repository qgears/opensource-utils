package hu.qgears.parser.tokenizer.recognizer;

import java.util.HashSet;
import java.util.Set;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerXtextId extends RecognizerConcat {

	private Character escapeChar='^';
	private static Set<Character> startCharacters=new HashSet<>();
	private static Set<Character> goonCharacters=new HashSet<>();
	{
		for(char c: "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray())
		{
			startCharacters.add(c);
		}
		for(char c: "1234567890".toCharArray())
		{
			goonCharacters.add(c);
		}
	}
	public RecognizerXtextId(ITokenType tokenType) {
		this(tokenType, null);
	}
	public RecognizerXtextId(ITokenType tokenType, Character startEscapeChar) {
		super(tokenType);
		addSubToken(new RecognizerIdStart(new TokenType("dummy"), startEscapeChar), true);
		addSubToken(new RecognizerIdInside(new TokenType("dummy")), false);
	}
	@Override
	public IToken getGeneratedToken(ITextSource src) {
		Character c=src.getCharAt(0);
		int at=0;
		if(c!=null && this.escapeChar!=null && this.escapeChar.charValue()==c.charValue())
		{
			at++;
		}
		Character c0=src.getCharAt(at);
		if(c0!=null && startCharacters.contains(c0))
		{
			at++;
			Character c1=src.getCharAt(at);
			while(c1!=null && (startCharacters.contains(c1) || goonCharacters.contains(c1)))
			{
				at++;
				c1=src.getCharAt(at);
			}
			return new SimpleToken(tokenType, src, at);
		}
		return null;
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
