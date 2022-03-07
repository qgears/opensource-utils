package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerStringInside extends RecognizerAbstract implements
		ITokenRecognizer {
	private char stringEndingCharacter='"';
	private char escapeCharacter='\\';
	public RecognizerStringInside(ITokenType tokenType, char stringEndingCharacter) {
		super(tokenType);
		this.stringEndingCharacter=stringEndingCharacter;
	}

	@Override
	public IToken getGeneratedToken(ITextSource src) {
		int ctr = 0;
		boolean lastEscape = false;
		while (src.getCharAt(ctr) != null
				&& (lastEscape || src.getCharAt(ctr) != stringEndingCharacter)) {
			if (!lastEscape && src.getCharAt(ctr) == escapeCharacter)
				lastEscape = true;
			else
				lastEscape = false;
			ctr++;
		}
		if (ctr > 0)
			return new SimpleToken(getTokenType(), src, ctr);
		else
			return null;
	}
}
