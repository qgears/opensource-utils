package hu.qgears.parser.tokenizer.recognizer;

import java.util.Collections;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerDoubleNumber implements ITokenRecognizer {
	private boolean acceptWholeNumber=true;
	private boolean acceptPrevAndFollowingDot=true;
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
	public RecognizerDoubleNumber(ITokenType type) {
		super();
		this.type = type;
	}

	@Override
	public IToken getGeneratedToken(ITextSource src) {
		int i=0;
		State state=State.init;
		loop:
		while(true)
		{
			Character c=src.getCharAt(i);
			if(c==null)
			{
				break;
			}
			switch(state)
			{
			case init:
				if(Character.isDigit(c))
				{
					// Valid Part of number
					state=State.hasWhole;
				}else if(c==partialSeparator)
				{
					state=State.hasDot;
				}else
				{
					break loop;
				}
				break;
			case hasDot:
				if(Character.isDigit(c))
				{
					// ok
				}else if(c=='e'||c=='E')
				{
					state=State.hasE;
				}else
				{
					break loop;
				}
				break;
			case hasE:
				if(Character.isDigit(c))
				{
					state=State.hasEDigit;
				}else if(c=='+'||c=='-')
				{
					state=State.hasESign;
				}else
				{
					break loop;
				}
				break;
			case hasESign:
			case hasEDigit:
				if(Character.isDigit(c))
				{
					state=State.hasEDigit;
				}else
				{
					break loop;
				}
				break;
			case hasPartial:
				if(Character.isDigit(c))
				{
					state=State.hasPartial;
				}else if(c=='e'||c=='E')
				{
					state=State.hasE;
				}else
				{
					break loop;
				}
			case hasWhole:
				if(Character.isDigit(c))
				{
					state=State.hasWhole;
				}
				else if(c==partialSeparator)
				{
					state=State.hasDot;
				}else if(c=='e'||c=='E')
				{
					state=State.hasE;
				}else
				{
					break loop;
				}
			}
			i++;
		}
		if(!acceptPrevAndFollowingDot)
		{
			Character c=src.getCharAt(i);
			if(c!=null)
			{
				if('.'==c || ':'==c)
				{
					return null;
				}
			}
			c=src.getCharAt(-1);
			if(c!=null)
			{
				if('.'==c || ':'==c || Character.isDigit(c))
				{
					return null;
				}
			}
		}
		switch(state)
		{
		case hasWhole:
			if(!acceptWholeNumber)
			{
				// Do not accept whole numbers
				return null;
			}
		case hasEDigit:
		case hasPartial:
			// Valid number
			return new SimpleToken(type, src, i);
		case hasDot:
			if(i>1)
			{
				// 1. is a valid number but . is not
				return new SimpleToken(type, src, i);
			}else
			{
				return null;
			}
		case hasE:
			// 1.0e is not a valid number
		case hasESign:
			// 1.0e+ or 1.0e- is not a valid number.
		case init:
			return null;
		}
		return null;
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
	public RecognizerDoubleNumber setAcceptWholeNumber(boolean acceptWholeNumber) {
		this.acceptWholeNumber = acceptWholeNumber;
		return this;
	}
	public RecognizerDoubleNumber setAcceptPrevAndFollowingDot(boolean b) {
		acceptPrevAndFollowingDot=b;
		return this;
	}
}
