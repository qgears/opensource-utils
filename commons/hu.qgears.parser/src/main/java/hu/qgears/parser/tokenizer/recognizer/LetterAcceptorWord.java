package hu.qgears.parser.tokenizer.recognizer;

public class LetterAcceptorWord implements ILetterAcceptor {
	public boolean accept(char ch) {
		return Character.isLetter(ch);
	}
}
