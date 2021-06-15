package hu.qgears.parser.language.impl;

import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;

/**
 * Generate number ids for token types.
 * 
 * @author rizsi
 * 
 */
public class IDGen {
	/**
	 * Generate number ids for token types.
	 * 
	 * @param types
	 */
	public void genTokenTypeIds(List<ITokenType> types) {
		for (int i = 0; i < types.size(); ++i) {
			types.get(i).setId(i);
		}
	}

	/**
	 * Generate number ids for token types of the recognizers.
	 * 
	 * @param types
	 */
	public void genTokenTypeIdsFromRecog(List<ITokenRecognizer> types) {
		int ctr = 0;
		for (ITokenRecognizer tr : types) {
			for (ITokenType t : tr.getRecognizedTokenTypes()) {
				t.setId(ctr++);
			}
		}
	}
}
