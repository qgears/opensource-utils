package hu.qgears.parser.language.impl;

import java.io.StringWriter;

import hu.qgears.parser.language.IAmbiguousSolver;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.ITermFilterDef;
import hu.qgears.parser.tokenizer.impl.TokenFilterDef;
import hu.qgears.parser.tokenizer.impl.TokenizerDef;



public class Language implements ILanguage {
	Term[] terms;
	String rootName;
	Term rootTerm;
	TokenizerDef tokenizerDef;
	private IAmbiguousSolver ambiguousSolver;

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public Term[] getTerms() {
		return terms;
	}

	public void setTerms(Term[] terms) {
		this.terms = terms;
	}

	public void setTokenizerDef(TokenizerDef tokenizerDef) {
		this.tokenizerDef = tokenizerDef;
	}


	public TokenizerDef getTokenizerDef() {
		return tokenizerDef;
	}

	TokenFilterDef tokenFilterDef;

	public TokenFilterDef getTokenFilterDef() {
		return tokenFilterDef;
	}

	public void setTokenFilterDef(TokenFilterDef tokenFilterDef) {
		this.tokenFilterDef = tokenFilterDef;
	}

	public Term getRootTerm() {
		return rootTerm;
	}

	public void setRootTerm(Term rootTerm) {
		this.rootTerm = rootTerm;
	}

	TermFilterDef termFilterDef = new TermFilterDef();

	public ITermFilterDef getTermFilterDef() {
		return termFilterDef;
	}

	@Override
	public String toString() {
		StringWriter ret = new StringWriter();
		toString(ret, "");
		return ret.toString();
	}

	public void toString(StringWriter ret, String prefix) {
		ret.append(prefix + "terms: \n");
		for (Term term : getTerms()) {
			term.toString(ret, prefix + "\t");
			ret.append("\n");
		}
	}
	@Override
	public IAmbiguousSolver getAmbiguousSolver() {
		return ambiguousSolver;
	}
	public void setAmbiguousSolver(IAmbiguousSolver ambiguousSolver) {
		this.ambiguousSolver = ambiguousSolver;
	}
}
