package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;

public class RecognizerEOF extends RecognizerAbstract implements
		ITokenRecognizer {
	@Override
	public int getGeneratedToken(char[] arr, int at) {
		return 0;
	}

	public RecognizerEOF(ITokenType tokenType) {
		super(tokenType);
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}

	@Override
	public boolean tokenCanStartWith(char c) {
		return false;
	}
}
