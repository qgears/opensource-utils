package hu.qgears.parser.language.impl;

import java.util.Map;

import hu.qgears.parser.language.EType;

/**
 * Symbol that references an other symbol
 * @author rizsi
 *
 */
public class TermRef extends Term {
	private Term sub;

	public String getReferenced() {
		return subsStr.get(0);
	}

	public TermRef(String name, String referenced) {
		super(name);
		subsStr.add(referenced);
	}

	@Override
	public EType getType() {
		return EType.reference;
	}

	@Override
	public String toString() {
		return "->" + getReferenced();
	}
	public void initialize(Map<String, Term> termMap) {
		sub = termMap.get(getReferenced());
	}

	public Term getSub() {
		return sub;
	}
}
