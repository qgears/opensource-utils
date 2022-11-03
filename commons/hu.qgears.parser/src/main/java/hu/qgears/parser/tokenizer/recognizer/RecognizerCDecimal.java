package hu.qgears.parser.tokenizer.recognizer;

import java.util.Collections;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.SimpleToken;

/**
 * Recognize decimal number with postfix allowed. (u, U, l, L)
 */
public class RecognizerCDecimal implements ITokenRecognizer {
	private ITokenType tokenType;
	private List<ITokenType> l;
	public RecognizerCDecimal(ITokenType tokenType) {
		this.tokenType=tokenType;
		l=Collections.singletonList(tokenType);
	}
	public static Number valueOf(String string) {
		return Long.parseLong(string);
	}
	@Override
	public IToken getGeneratedToken(ITextSource src) {
		int ctr = 0;
		char ch = src.getCharAt(ctr);
		int l=src.getLength();
		while (Character.isDigit(ch)) {
			ctr++;
			if(l<=ctr)
			{
				break;
			}
			ch = src.getCharAt(ctr);
		}
		if (ctr == 0)
			return null;
		if(l>ctr)
		{
			ch = src.getCharAt(ctr);
			if(ch=='u'||ch=='U')
			{
				ctr++;
			}
		}
		if(l>ctr)
		{
			ch = src.getCharAt(ctr);
			if(ch=='l'||ch=='L')
			{
				ctr++;
			}
		}
		return new SimpleToken(tokenType, src, ctr);
	}
	@Override
	public List<ITokenType> getRecognizedTokenTypes() {
		return l;
	}
	@Override
	public Matcher createMatcher(String matchingString) {
		return new Matcher(true, matchingString);
	}
}
