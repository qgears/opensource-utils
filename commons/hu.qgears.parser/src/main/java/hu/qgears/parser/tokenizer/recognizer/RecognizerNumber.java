package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;

public class RecognizerNumber extends RecognizerAnyLetter {
	public RecognizerNumber(ITokenType tokenType) {
		super(tokenType, new LetterAcceptorNumber());
	}
	public static Number valueOf(String string) {
		return Long.parseLong(string);
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
		collector.accept("decimalNumber");
		super.collectPorposals(tokenTypeName, prefix, collector);
	}
}
