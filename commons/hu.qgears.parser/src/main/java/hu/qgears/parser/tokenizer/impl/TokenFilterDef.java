package hu.qgears.parser.tokenizer.impl;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hu.qgears.parser.tokenizer.ITokenFilterDef;



public class TokenFilterDef implements ITokenFilterDef {
	Set<String> toFilter = new TreeSet<String>();

	public Set<String> getToFilter() {
		return toFilter;
	}

	public void setToFilter(Set<String> toFilter) {
		this.toFilter = toFilter;
	}

	public TokenFilterDef(List<String> toFilter) {
		super();
		this.toFilter.addAll(toFilter);
	}
}
