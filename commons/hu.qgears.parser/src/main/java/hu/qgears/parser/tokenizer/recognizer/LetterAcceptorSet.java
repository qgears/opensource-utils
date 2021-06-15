package hu.qgears.parser.tokenizer.recognizer;

import java.util.Set;
import java.util.TreeSet;

public class LetterAcceptorSet implements ILetterAcceptor {
	Set<Character> acceptedChars = new TreeSet<Character>();

	public boolean accept(char ch) {
		return acceptedChars.contains(ch);
	}

	public LetterAcceptorSet(Set<Character> acceptedChars) {
		super();
		this.acceptedChars = acceptedChars;
	}

	public LetterAcceptorSet(Character[] acceptedChars) {
		super();
		for (Character c : acceptedChars) {
			this.acceptedChars.add(c);
		}
	}
}
