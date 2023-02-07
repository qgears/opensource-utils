package hu.qgears.parser.contentassist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import hu.qgears.parser.language.impl.Term;

/**
 * Possible content assist reply at a given position.
 */
public class PossibleGoon implements Consumer<String> {
	public Term t;
	
	public PossibleGoon(Term t) {
		super();
		this.t = t;
	}
	public List<String> prefixes=new ArrayList<>();
	public void addContentAssistProposal(String s) {
		prefixes.add(s);
	}
	@Override
	public void accept(String proposal) {
		addContentAssistProposal(proposal);
	}
}
