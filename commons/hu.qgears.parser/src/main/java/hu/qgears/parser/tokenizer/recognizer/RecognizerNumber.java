package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerNumber extends RecognizerAbstract {
	public RecognizerNumber(ITokenType tokenType) {
		super(tokenType);
	}
	public static Number valueOf(String string) {
		return Long.parseLong(string);
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
		collector.accept("decimalNumber");
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
			if(!Character.isDigit(ch))
			{
				return i;
			}
		}
		return i;
	}
}
