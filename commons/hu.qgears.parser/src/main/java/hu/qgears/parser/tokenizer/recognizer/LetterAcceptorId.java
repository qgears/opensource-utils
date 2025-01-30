package hu.qgears.parser.tokenizer.recognizer;

public class LetterAcceptorId {
	public static boolean accept(char ch) {
		return Character.isJavaIdentifierPart(ch);
	}
}
