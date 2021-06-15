package hu.qgears.parser.language.impl;

import hu.qgears.parser.language.EType;

public class TermOr extends TermCompound {

	public TermOr(String name) {
		super(name);
	}

	@Override
	public EType getType() {
		return EType.or;
	}

}
