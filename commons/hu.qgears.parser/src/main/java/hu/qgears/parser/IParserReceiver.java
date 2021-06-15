package hu.qgears.parser;

import java.util.List;

import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.ParseException;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.tokenizer.IToken;

/**
 * Interface to receive additional information when a parsing is executed.
 */
public interface IParserReceiver {
	void stucked(ElemBuffer buffer, IToken t) throws ParseException;

	void parseProblemUnknown(ElemBuffer buffer) throws ParseException;

	void treeUnfiltered(TreeElem root);

	void treeFiltered(TreeElem root);

	void tokensUnfiltered(List<IToken> tokensUnfiltered);

	void tokens(List<IToken> tokens);
}
