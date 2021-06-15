package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.qgears.parser.util.UtilString;



abstract public class TermCompound extends Term {
	public TermCompound(String name) {
		super(name);
	}

	List<Term> subs = new ArrayList<Term>();

	public List<Term> getSubs() {
		return subs;
	}

	@Override
	public String toString() {
		return ""+getType().toString()
				+ UtilString.concat("(", getSubsStr(), ",", ")");
	}

	public void initialize(Map<String, Term> termMap) {
		subs=new ArrayList<Term>();
		for (String s : getSubsStr()) {
			getSubs().add(termMap.get(s));
		}
	}

	public String getReferenced() {
		return getName();
	}
}
