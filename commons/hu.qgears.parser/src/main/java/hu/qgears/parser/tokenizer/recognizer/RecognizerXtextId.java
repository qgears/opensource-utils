package hu.qgears.parser.tokenizer.recognizer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerXtextId extends RecognizerAbstract {

	private char escapeChar='^';
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
	private static boolean isStartCharacter(char ch)
	{
		return ch=='_'||(ch>='a' && ch<='z') ||(ch>='A' && ch<='Z');
	}
	private static boolean isGoonCharacter(char ch)
	{
		return (ch>='0' && ch<='9');
	}
	public RecognizerXtextId(ITokenType tokenType) {
		this(tokenType, null);
	}
	public RecognizerXtextId(ITokenType tokenType, Character startEscapeChar) {
		super(tokenType);
	}
	@Override
	public int getGeneratedToken(TextSource src) {
		int pos=src.getPosition();
		int len=src.getLength();
		char[] arr=src.array;
		char c=arr[pos];
		int at=0;
		if(this.escapeChar==c)
		{
			at++;
		}
		if(pos+at<len)
		{
			char c0=arr[pos+at];
			if(isStartCharacter(c0))
			{
				for(;at+pos<len;++at)
				{
					char c1=arr[pos+at];
					if(!(isStartCharacter(c1) || isGoonCharacter(c1)))
					{
						break;
					}
				}
				return at;
			}
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
