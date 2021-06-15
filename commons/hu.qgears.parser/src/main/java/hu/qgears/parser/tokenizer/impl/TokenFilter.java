package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenFilterDef;



public class TokenFilter {
	ITokenFilterDef tokenFilterDef;

	public TokenFilter(ITokenFilterDef tokenFilterDef) {
		this.tokenFilterDef = tokenFilterDef;
	}

	public List<IToken> filter(List<IToken> toks) {
		List<IToken> ret = new ArrayList<IToken>();
		for (IToken t : toks) {
			if (!tokenFilterDef.getToFilter().contains(
					t.getTokenType().getName())) {
				ret.add(t);
			}
		}
		return ret;
	}

}
