package hu.qgears.parser;

import java.util.List;

import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.ParseException;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.TokenizerException;

/**
 * Interface to receive additional information when a parsing is executed.
 */
public interface IParserReceiver {
	/**
	 * Parsing stucked at the given token. This token does not fit any
	 * grammar rule and the AST tree can not be built.
	 * 
	 * Parsing is stopped after this call.
	 * 
	 * @param buffer the result of filling the parse buffer
	 * @param t the token that can not fit any grammar rule at its location.
	 * @throws ParseException
	 */
	default void stucked(ElemBuffer buffer, IToken t) throws ParseException { }
	/**
	 * Internal error in the parser or a specific case that is not properly handled by the parser yet.
	 * 
	 * Users should just show an error marker on the document that it is not parseable.
	 * If this ever happens then the case should be detected and better error reporting implemented.
	 * @param buffer
	 * @throws ParseException
	 */
	default void parseProblemUnknown(ElemBuffer buffer) throws ParseException{}
	/**
	 * The AST tree just after finished parsing and before filtering it.
	 * 
	 * This is useful when the fine structure of the document is to be detected.
	 * For example source code coloring uses this feature.
	 * @param root
	 */
	default void treeUnfiltered(TreeElem root) {}

	/**
	 * The final result AST of the parsing. This contains all information for interpreting the source code.
	 *
	 * Filtering is useful to omit details of the AST tree that were useful for the parser
	 * but are only noise while interpreting the AST.
	 * The filtered nodes are referring grammar nodes that are not marked to remain in the result
	 * and grammar nodes that were implicitly created while interpreting the grammar.
	 *
	 * @param root
	 */
	default void treeFiltered(TreeElem root) {}

	/**
	 * Receive the unfiltered list of parsed tokens.
	 * @param tokensUnfiltered the internal array is passed but it should be handled read only
	 */
	default void tokensUnfiltered(List<IToken> tokensUnfiltered) {}

	/**
	 * Receive the filtered list of parsed tokens.
	 * The receiver may modify the list and modifying it will have an effect on the ongoing parse process.
	 * @param tokensUnfiltered  the internal array is passed but it should be handled read only
	 */
	default void tokens(List<IToken> tokens) {}

	/**
	 * Called in case tokenization finished with an error.
	 * Parsing will not be stopped after this call.
	 * @param exc
	 * @throws TokenizerException
	 */
	default void tokenizeError(TokenizerException exc) throws TokenizerException {}

	/**
	 * Called when filling the parse buffer was finished.
	 * This is the step before building the AST.
	 * It is possible that the parse buffer does not construct a valid AST tree.
	 * When the beginning substring of a valid document is parsed then at this point we will have
	 * a useful result that can be used to extract how the document could be continued in a valid
	 * way that fits the grammar.
	 * 
	 * This method is used by content assist to collect possible next token of the document.
	 * @param buffer
	 * @param size
	 */
	default void tableFilled(ElemBuffer buffer, int size) {}
}
