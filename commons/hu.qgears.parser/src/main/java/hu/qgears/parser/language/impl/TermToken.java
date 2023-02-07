package hu.qgears.parser.language.impl;

import java.util.Map;

import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.Matcher;

/**
 * symbol that references a token.
 */
public class TermToken extends Term {
	private ITokenType tokenType;
	private Matcher matchingValue;

	public TermToken(String name, ITokenType tokenType, Matcher matchingValue) {
		super(name);
		this.tokenType = tokenType;
		this.matchingValue=matchingValue;
	}

	@Override
	public EType getType() {
		return EType.token;
	}

	public ITokenType getTokenType() {
		return tokenType;
	}

	public void initialize(Map<String, Term> termMap) {
	}

	public String getReferenced() {
		return getName();
	}
	/**
	 * Get matcher if exists.
	 * @return
	 */
	public Matcher getMatchingValue() {
		return matchingValue;
	}
	
	@Override
	public String toString() {
		return super.toString()+" '"+tokenType.getName()+"'"+" '"+matchingValue+"'";
	}

}
