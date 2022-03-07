package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerComment extends RecognizerConcat {
	private char exit0;
	private char exit1;
	class RecognizerCommentInside extends RecognizerAbstract {
		public RecognizerCommentInside() {
			super(new TokenType("dummy"));
		}

		@Override
		public IToken getGeneratedToken(ITextSource src) {
			int ctr = 0;
			while (true) {
				Character cho = src.getCharAt(ctr);
				if (cho == null) {
					break;
				}
				char ch = cho.charValue();
				if (ch == exit0) {
					Character chh = src.getCharAt(ctr + 1);
					if (chh != null && chh.charValue() == exit1) {
						break;
					}
				}
				ctr++;
			}
			return new SimpleToken(tokenType, src, ctr);
		}

	}

	@Override
	public IToken getGeneratedToken(ITextSource _src) {
		return super.getGeneratedToken(_src);
	}

	public RecognizerComment(ITokenType tokenType, String open, String close) {
		super(tokenType);
		if(close.length()!=2)
		{
			throw new IllegalArgumentException("commend close string must have length of 2: '"+close+"'");
		}
		exit0=close.charAt(0);
		exit1=close.charAt(1);
		addSubToken(new RecognizerConst(new TokenType("dummy"), open), true);
		addSubToken(new RecognizerCommentInside(), false);
		addSubToken(new RecognizerConst(new TokenType("dummy"), close), true);
	}
	public RecognizerComment(ITokenType tokenType) {
		this(tokenType, "/*", "*/");
	}

}
