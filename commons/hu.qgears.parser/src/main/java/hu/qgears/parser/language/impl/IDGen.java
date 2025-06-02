package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.impl.TokenizerDef;

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
	public void genTokenTypeIdsFromRecog(ILanguage language) {
		List<ITokenRecognizer> types = language.getTokenizerDef().getRecognizers();
		int ctr = 0;
		List<ITokenType> flatTypes=new ArrayList<ITokenType>();
		for (ITokenRecognizer tr : types) {
			ITokenType t = tr.getRecognizedTokenType();
			{
				flatTypes.add(t);
				t.setId(ctr++);
			}
		}
		((TokenizerDef)language.getTokenizerDef()).setFlatTypesArray(flatTypes);
		if(language.getTokenFilterDef()!=null)
		{
			language.getTokenFilterDef().setupIds(flatTypes);
		}
	}
}
