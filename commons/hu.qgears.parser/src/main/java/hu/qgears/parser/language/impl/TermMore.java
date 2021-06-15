package hu.qgears.parser.language.impl;

import java.util.Map;

import hu.qgears.parser.tokenizer.impl.LanguageParseException;

/**
 * Abstract Symbol that means more instances of a symbol.
 * @author rizsi
 *
 */
abstract public class TermMore extends Term {
	private Term sub;

	public TermMore(String name, String referenced)
			throws LanguageParseException {
		super(name);
		if (referenced == null) {
			throw new LanguageParseException(
					"null reference is not allowed for: " + name);
		}
		subsStr.add(referenced);
	}

	public String getReferencedMore() {
		return subsStr.get(0);
	}

	public String getReferenced() {
		return name;
	}
	public Term getSub() {
		return sub;
	}

	public void initialize(Map<String, Term> termMap) {
		sub = termMap.get(getReferencedMore());
	}
}
