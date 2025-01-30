package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.impl.TextSource;

/**
 * Recognize decimal number with postfix allowed. (u, U, l, L)
 */
public class RecognizerCDecimal implements ITokenRecognizer {
	private ITokenType tokenType;
	public RecognizerCDecimal(ITokenType tokenType) {
		this.tokenType=tokenType;
	}
	public static Number valueOf(String string) {
		return Long.parseLong(string);
	}
	@Override
	public int getGeneratedToken(TextSource src) {
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
			return 0;
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
		return ctr;
	}
	@Override
	public ITokenType getRecognizedTokenTypes() {
		return tokenType;
	}
	@Override
	public Matcher createMatcher(String matchingString) {
		return new Matcher(true, matchingString);
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
		collector.accept("decimalNumber");
	}
}
