package hu.qgears.parser.tokenizer.recognizer;

public class LetterAcceptorNumber implements ILetterAcceptor {
	public boolean accept(char ch) {
		return Character.isDigit(ch);
	}
}
