package hu.qgears.parser.tokenizer.impl;

import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenizerDef;



public class TokenizerDef implements ITokenizerDef {

	List<ITokenRecognizer> recognizers;
	ITokenType eof;
	private ITokenType[] tokenTypesArray;

	public ITokenType getEof() {
		return eof;
	}

	public void setEof(ITokenType eof) {
		this.eof = eof;
	}

	public List<ITokenRecognizer> getRecognizers() {
		return recognizers;
	}

	public TokenizerDef(List<ITokenRecognizer> recognizers) {
		super();
		this.recognizers = recognizers;
	}

	@Override
	public ITokenType tokenTypeById(int type) {
		return tokenTypesArray[type];
	}
	public void setFlatTypesArray(List<ITokenType> flatTypes)
	{
		tokenTypesArray=flatTypes.toArray(new ITokenType[] {});
	}
}
