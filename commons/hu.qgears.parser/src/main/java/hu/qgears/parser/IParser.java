package hu.qgears.parser;

import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.ParseException;
import hu.qgears.parser.tokenizer.TokenArray;

/** Interface for a parser instance.
 */
public interface IParser {

	TokenArray tokenize(IParserReceiver receiver) throws ParseException;
	TokenArray getTokensUnfiltered();

	/**
	 * Execute parsing of current input.
	 * @param receiver intermediate and internal information is passed to this object
	 * @return AST as result of parsing the text.
	 * @throws ParseException
	 */
	ITreeElem parse(IParserReceiver receiver) throws ParseException;
	/**
	 * Set the buffer to use when parsing.
	 * This feature is useful to spare memory garbage collection when the same parser is re-used many times.
	 * @param buffer re-use this buffer when parsing.
	 */
	void setBuffer(ElemBuffer buffer);

}
