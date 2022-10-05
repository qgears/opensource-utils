package hu.qgears.parser.contentassist;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.language.impl.Term;

public class PossibleGoon {
	public Term t;
	
	public PossibleGoon(Term t) {
		super();
		this.t = t;
	}
	public List<String> prefixes=new ArrayList<>();
	public void add(String s) {
		prefixes.add(s);
	}
	public void add(String packageName, String string) {
		prefixes.add(packageName);
	}
}
