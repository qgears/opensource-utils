package hu.qgears.parser.language.impl;

import hu.qgears.parser.language.EType;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;

/**
 * One or more instance of a symbol.
 * @author rizsi
 *
 */
public class TermOneOrMore extends TermMore {

	public TermOneOrMore(String name, String referenced)
			throws LanguageParseException {
		super(name, referenced);
	}

	@Override
	public EType getType() {
		return EType.oneormore;
	}

}
