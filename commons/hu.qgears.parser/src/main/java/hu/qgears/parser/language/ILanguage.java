package hu.qgears.parser.language;

import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.tokenizer.impl.TokenFilterDef;
import hu.qgears.parser.tokenizer.impl.TokenizerDef;

/**
 * The grammar of a language.
 * The language can be used to instantiate parsers.
 * @author rizsi
 *
 */
public interface ILanguage {
	/**
	 * The root non-terminal of the language.
	 * 
	 * @return
	 */
	Term getRootTerm();

	/**
	 * Get the tokenizer definition of the language
	 * @return
	 */
	TokenizerDef getTokenizerDef();

	/**
	 * Get the tokenizer filter definition of the language.
	 * @return
	 */
	TokenFilterDef getTokenFilterDef();

	/**
	 * Non-Terminals (terms) of the language..
	 * 
	 * @return
	 */
	Term[] getTerms();
	/**
	 * Get the term filter definition.
	 * @return
	 */
	ITermFilterDef getTermFilterDef();
}
