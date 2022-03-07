package hu.qgears.parser.tokenizer.recognizer;

import java.util.Collections;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.SimpleToken;

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
	public IToken getGeneratedToken(ITextSource src) {
		if(src.startsWith(0, "0x")|| src.startsWith(0, "0X"))
		{
			int i=2;
			while(validHexaChar(src.getCharAt(i)))
			{
				i++;
			}
			if(i>2)
			{
				return new SimpleToken(type, src, i);
			}
		}
		return null;
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
	public List<ITokenType> getRecognizedTokenTypes() {
		return Collections.singletonList(type);
	}

	@Override
	public Matcher createMatcher(String matchingString) {
		// This feature is not supported
		return null;
	}
}
