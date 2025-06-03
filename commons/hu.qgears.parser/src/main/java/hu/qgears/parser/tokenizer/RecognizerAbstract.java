package hu.qgears.parser.tokenizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;



public abstract class RecognizerAbstract implements ITokenRecognizer {
	protected ITokenType tokenType;

	abstract public int getGeneratedToken(char [] array, int at);

	public RecognizerAbstract(ITokenType tokenType) {
		super();
		this.tokenType = tokenType;
	}

	public ITokenType getRecognizedTokenType() {
		return tokenType;
	}

	final public ITokenType getTokenType() {
		return tokenType;
	}
	final public int getTokenTypeId() {
		return tokenType.getId();
	}
	@Override
	public Matcher createMatcher(String matchingString) {
		return new Matcher(true, matchingString);
	}
}
