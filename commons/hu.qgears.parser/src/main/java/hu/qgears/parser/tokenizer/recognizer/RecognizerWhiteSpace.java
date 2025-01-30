package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerWhiteSpace extends RecognizerAbstract {
	public RecognizerWhiteSpace(ITokenType tokenType) {
		super(tokenType);
		//, new Character[] { ' ', '\n', '\t', '\r' }
	}

	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}

	@Override
	public int getGeneratedToken(TextSource src) {
		int pos=src.getPosition();
		int max=src.getLength()-pos;
		char[] arr=src.array;
		int i=0;
		for(;i<max;++i)
		{
			char ch = arr[pos+i];
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
}
