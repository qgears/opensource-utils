package hu.qgears.parser.contentassist;

import java.util.List;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermToken;
import hu.qgears.parser.tokenizer.IToken;

public interface ICompletitionProposalContext {
	/**
	 * Parse the text and call back the given callback while doing that.
	 * @param textPreCursor
	 * @param iParserReceiver
	 * @throws Exception 
	 */
	void parse(String text, IParserReceiver iParserReceiver) throws Exception;
	/**
	 * Is the given token filtered.
	 * @param t
	 * @return
	 */
	boolean isFiltered(IToken t);
	/**
	 * Collect content assist proposals at the current AST node with a given prefix.
	 * @param collect collector where context assist proposals are added to.
	 * @param t Current AST node
	 * @param buffer the current parse result
	 * @param prefix 
	 * @param context
	 * @param parents 
	 * @return true means further travrsal of the AST tree is stopped
	 */
	boolean collectAllowedPrefixes(PossibleGoon collect, Term t, ElemBuffer buffer, String prefix, List<String> context, List<Term> parents);
	/**
	 * Error happened during preparing content assist result.
	 * Exception can be logged or somehow shown to the user.
	 * @param e
	 */
	void logError(Exception e);

	/**
	 * When entering an AST node this is called.
	 * Can be used to collect the current state of AST. Empy implementation is ok when state need not be collected.
	 * @param tree
	 * @param depth
	 * @return returne closeable is closed when leaving the AST node.
	 */
	default AutoCloseable collectCurrentStateOfText(ITreeElem tree, int depth)
	{
		return ()->{};
	}
	/**
	 * Based on tokentype generate possible context assist proposals.
	 * @param collect proposals are collected into this object.
	 * @param tokenType the type to generate proposals for
	 * @param prefix prefix of existing token
	 */
	void collectPossibilities(PossibleGoon collect, TermToken termType, String prefix);
}
