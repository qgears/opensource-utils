package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerIdStart extends RecognizerAbstract {
	Character escapeChar;
	public RecognizerIdStart(ITokenType tokenType, Character escapeChar) {
		super(tokenType);
		this.escapeChar=escapeChar;
	}

	@Override
	public IToken getGeneratedToken(ITextSource src) {
		Character c=src.getCharAt(0);
		if(c!=null)
		{
			char ch0=c;
			if(this.escapeChar!=null)
			{
				if(src.getCharAt(0).charValue()==escapeChar.charValue())
				{
					c=src.getCharAt(1);
					if(c!=null)
					{
						ch0=c;
						if(Character.isJavaIdentifierStart(ch0))
						{
							return new SimpleToken(tokenType, src, 2);
						}
					}
				}
			}
			if(Character.isJavaIdentifierStart(ch0))
			{
				return new SimpleToken(tokenType, src, 1);
			}
		}
		return null;
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}
}
