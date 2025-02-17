package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;

public class RecognizerWhiteSpace extends RecognizerAbstract {
	public RecognizerWhiteSpace(ITokenType tokenType) {
		super(tokenType);
		//, new Character[] { ' ', '\n', '\t', '\r' }
	}

	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}

	@Override
	public int getGeneratedToken(char[] arr, int at) {
		int max=arr.length-at;
		int i=0;
		for(;i<max;++i)
		{
			char ch = arr[at+i];
			switch(ch)
			{
			case ' ':
			case '\n':
			case '\t':
			case '\r':
				break;
			default:
				return i;
			}
		}
		return i;
	}
	@Override
	public boolean tokenCanStartWith(char c) {
		return Character.isWhitespace(c);
	}
}
