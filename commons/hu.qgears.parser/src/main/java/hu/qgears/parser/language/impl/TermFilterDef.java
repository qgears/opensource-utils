package hu.qgears.parser.language.impl;

import java.util.Set;
import java.util.TreeSet;

import hu.qgears.parser.language.ITermFilterDef;



public class TermFilterDef implements ITermFilterDef {
	Set<String> remainingTerms = new TreeSet<String>();

	public Set<String> getRemainingTerms() {
		return remainingTerms;
	}
}
