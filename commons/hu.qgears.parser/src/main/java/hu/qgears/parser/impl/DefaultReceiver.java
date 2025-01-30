package hu.qgears.parser.impl;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.tokenizer.Token;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.TokenizerException;

public class DefaultReceiver implements IParserReceiver
{
	public TreeElem treeFiltered;
	@Override
	public void stucked(ElemBuffer buffer, Token t) throws ParseException {
		throw new ParseException("parsing stucked at: "+t.getTokenType().getName()
				+" '"
				+ t.getSource().lastChars(t.getPos(), 20) + "'|'"
				+ t.getSource().firstChars(t.getPos(), 20) + "'"
				+ "...").setPosition(t.getPos());
	}

	@Override
	public void parseProblemUnknown(ElemBuffer buffer) throws ParseException {
		// Parse is not succesful!
		// TODO should give some handle for finding the problem!
		// buffer.printCurrentGroup(System.err);
		throw new ParseException("Unexpected end of document");
	}

	@Override
	public void treeUnfiltered(TreeElem root) {
	}

	@Override
	public void treeFiltered(TreeElem root) {
		this.treeFiltered=root;
	}

	@Override
	public void tokensUnfiltered(TokenArray tokensUnfiltered) {
	}

	@Override
	public void tokens(TokenArray tokens) {
	}

	@Override
	public void tokenizeError(TokenizerException exc) throws TokenizerException {
		throw exc;
	}
}
