package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.ITreeElem;

/**
 * Build a token filter from AST.
 */
public class TokenFilterParserAST {
	public TokenFilterDef parse(ITreeElem root) throws LanguageParseException {
		List<String> toFilter = new ArrayList<String>();
		for (ITreeElem e : root.getSubs()) {
			String name = e.getTypeName();
			if ("defFilterToken".equals(name)) {
				String na = e.getSubs().get(0).getString();
				toFilter.add(na);
			}
		}
		return new TokenFilterDef(toFilter);
	}
}
