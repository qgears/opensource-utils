package hu.qgears.parser.contentassist;

import java.util.List;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.language.impl.Term;
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
	 * TODO document.
	 * @param collect
	 * @param t
	 * @param buffer 
	 * @param prefix
	 * @param context
	 * @param parents 
	 * @return true means further travrsal of the AST tree is stopped
	 */
	boolean collectAllowedPrefixes(PossibleGoon collect, Term t, ElemBuffer buffer, String prefix, List<String> context, List<Term> parents);
	/**
	 * Error happened during preparing content assist result.
	 * @param e
	 */
	void logError(Exception e);
	/**
	 * TODO document
	 * @param tree
	 * @param depth
	 * @return
	 */
	default AutoCloseable collectCurrentStateOfText(ITreeElem tree, int depth)
	{
		return ()->{};
	}
}
