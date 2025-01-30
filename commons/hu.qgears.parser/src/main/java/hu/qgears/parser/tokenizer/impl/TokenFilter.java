package hu.qgears.parser.tokenizer.impl;

import hu.qgears.parser.tokenizer.TokenArray;

public class TokenFilter {
	private TokenFilterDef tokenFilterDef;

	public TokenFilter(TokenFilterDef tokenFilterDef) {
		this.tokenFilterDef = tokenFilterDef;
	}

	public TokenArray filter(TokenArray toks) {
		TokenArray ret=new TokenArray(toks.getSource(), toks.getLanguage());
		for(int i=0;i<toks.size();++i)
		{
			int type=toks.type(i);
			if(!tokenFilterDef.contains(type))
			{
				ret.addToken(type, toks.pos(i), toks.length(i));
			}
		}
		return ret;
	}

}
