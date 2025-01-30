package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerCStyleHexa implements ITokenRecognizer {
	ITokenType type;
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
	public int getGeneratedToken(TextSource src) {
		if(src.startsWith(0, "0x")|| src.startsWith(0, "0X"))
		{
			int i=2;
			while(validHexaChar(src.getCharAt(i)))
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
}
