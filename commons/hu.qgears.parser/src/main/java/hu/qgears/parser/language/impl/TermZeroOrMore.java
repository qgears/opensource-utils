package hu.qgears.parser.language.impl;

import hu.qgears.parser.language.EType;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;

public class TermZeroOrMore extends TermMore {

	public TermZeroOrMore(String name, String referenced)
			throws LanguageParseException {
		super(name, referenced);
	}

	@Override
	public EType getType() {
		return EType.zeroormore;
	}
	@Override
	public String toString() {
		return "*"+getSub().getName();
	}
}
