package hu.qgears.parser.tokenizer.recognizer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerXtextId extends RecognizerAbstract {

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
	}
	@Override
	public int getGeneratedToken(TextSource src) {
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
			return at;
		}
		return 0;
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
}
