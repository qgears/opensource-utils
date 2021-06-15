package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerStringInside extends RecognizerAbstract implements
		ITokenRecognizer {
	public RecognizerStringInside(ITokenType tokenType) {
		super(tokenType);
	}

	@Override
	public IToken getGeneratedToken(ITextSource src) {
		int ctr = 0;
		boolean lastEscape = false;
		while (src.getCharAt(ctr) != null
				&& (lastEscape || src.getCharAt(ctr) != '"')) {
			if (!lastEscape && src.getCharAt(ctr) == '\\')
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
