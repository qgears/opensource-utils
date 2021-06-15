package hu.qgears.parser.language.impl;

import java.util.Map;

import hu.qgears.parser.language.EType;

/**
 * The nothing symbol.
 * @author rizsi
 *
 */
public class TermEpsilon extends Term {

	public TermEpsilon(String name) {
		super(name);
	}

	@Override
	public EType getType() {
		return EType.epsilon;
	}

	public void initialize(Map<String, Term> termMap) {
	}

	public String getReferenced() {
		return getName();
	}

	@Override
	public boolean isFiltered() {
		return true;
	}

}
