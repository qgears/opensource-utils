package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerAnyLetter extends RecognizerAbstract implements
		ITokenRecognizer {
	ILetterAcceptor acceptor;

	@Override
	public int getGeneratedToken(TextSource src) {
		int ctr = 0;
		char ch = src.getCharAt(ctr);
		while (acceptor.accept(ch)) {
			ctr++;
			Character c = src.getCharAt(ctr);
			if (c == null)
				break;
			ch = c;
		}
		if (ctr == 0)
			return 0;
		return ctr;
	}

	public RecognizerAnyLetter(ITokenType tokenType, ILetterAcceptor acceptor) {
		super(tokenType);
		this.acceptor = acceptor;
	}

	public RecognizerAnyLetter(ITokenType tokenType, Character[] acceptedChars) {
		super(tokenType);
		this.acceptor = new LetterAcceptorSet(acceptedChars);
	}

	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}
}
