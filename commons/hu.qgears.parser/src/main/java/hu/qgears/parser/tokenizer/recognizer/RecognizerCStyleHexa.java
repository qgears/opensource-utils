package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerCStyleHexa implements ITokenRecognizer {
	ITokenType type;
	char[] prefix1="0x".toCharArray();
	char[] prefix2="0X".toCharArray();
	enum State
	{
		init,
		hasWhole,
		hasDot,
		hasPartial,
		hasE,
		hasESign,
		hasEDigit,
	}
	char partialSeparator='.';
	public RecognizerCStyleHexa(ITokenType type) {
		super();
		this.type = type;
	}

	@Override
	public int getGeneratedToken(char[] arr, int at) {
		if(TextSource.startsWith(arr, at, prefix1)|| TextSource.startsWith(arr, at, prefix2))
		{
			int i=2;
			while(validHexaChar(TextSource.getCharAt(at, arr, i)))
			{
				i++;
			}
			if(i>2)
			{
				return i;
			}
		}
		return 0;
	}

	private boolean validHexaChar(Character charAt) {
		if(charAt!=null)
		{
			char c=charAt;
			return (('a'<=c && 'f'>=c) || ('A'<=c && 'F'>=c) || ('0'<=c && '9'>=c));
		}
		return false;
	}

	@Override
	public ITokenType getRecognizedTokenTypes() {
		return type;
	}

	@Override
	public Matcher createMatcher(String matchingString) {
		// This feature is not supported
		return null;
	}
	public static long valueOf(String s)
	{
		return Long.parseLong(s.substring(2), 16);
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
		collector.accept("0xhexa");
	}
	@Override
	public boolean tokenCanStartWith(char c) {
		return c == prefix1[0] || c == prefix2[0];
	}
}
