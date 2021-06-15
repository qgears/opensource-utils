package hu.qgears.parser.tokenizer.recognizer;

public class LetterAcceptorIdFirst implements ILetterAcceptor {
	public boolean accept(char ch) {
		return Character.isJavaIdentifierStart(ch);
	}
}
