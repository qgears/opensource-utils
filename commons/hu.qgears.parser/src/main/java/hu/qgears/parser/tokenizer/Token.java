package hu.qgears.parser.tokenizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class Token {
	ITokenType type;
	TextSource source;
	int pos;
	int length;

	public Token(ITokenType type, TextSource source, int pos, int length) {
		super();
		this.type = type;
		this.source = source;
		this.pos = pos;
		this.length = length;
	}

	public Token(ITokenType type, TextSource source, int length) {
		super();
		this.type = type;
		this.source = source;
		this.pos = source.getPosition();
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public CharSequence getText() {
		return source.getFullSequence().subSequence(pos, pos + length);
	}

	public ITokenType getTokenType() {
		return type;
	}

	@Override
	public String toString() {
		return "'" + getText() + "' " + type;
	}

	public int getPos() {
		return pos;
	}

	public TextSource getSource() {
		return source;
	}
}
