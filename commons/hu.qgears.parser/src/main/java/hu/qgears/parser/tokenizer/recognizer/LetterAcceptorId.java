package hu.qgears.parser.tokenizer.recognizer;

public class LetterAcceptorId implements ILetterAcceptor {
	public boolean accept(char ch) {
		return Character.isJavaIdentifierPart(ch);
	}
}
