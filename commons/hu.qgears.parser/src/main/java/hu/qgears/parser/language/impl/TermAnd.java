package hu.qgears.parser.language.impl;

import hu.qgears.parser.language.EType;

/**
 * The non terminal that is a concatenation of two other non-terminals.
 * @author rizsi
 *
 */
public class TermAnd extends TermCompound {

	public TermAnd(String name) {
		super(name);
	}

	@Override
	public EType getType() {
		return EType.and;
	}
}
