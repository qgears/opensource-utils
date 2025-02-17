package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;

final public class TokenizerDef {
	private static final int nToCache = 256;
	private List<ITokenRecognizer> recognizers;
	private ITokenRecognizer[] recognizersAsArray;
	private ITokenType eof;
	private ITokenType[] tokenTypesArray;
	private ITokenRecognizer[][] starts = new ITokenRecognizer[nToCache][];

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
		for(char c = 0; c < nToCache; ++c)
		{
			List<ITokenRecognizer> canStartWithChar = new ArrayList<>();
			for(ITokenRecognizer tr: recognizers)
			{
				if(tr.tokenCanStartWith(c))
				{
					canStartWithChar.add(tr);
				}
			}
			starts[c] = canStartWithChar.toArray(new ITokenRecognizer[0]);
		}
		recognizersAsArray = recognizers.toArray(new ITokenRecognizer[0]);
	}
	public ITokenRecognizer[] getRecognizers(char c)
	{
		if(c < nToCache && c >= 0)
		{
			return starts[c];
		}
		else
		{
			return recognizersAsArray;
		}
	}

	public ITokenType tokenTypeById(int type) {
		return tokenTypesArray[type];
	}
	public void setFlatTypesArray(List<ITokenType> flatTypes)
	{
		tokenTypesArray=flatTypes.toArray(new ITokenType[] {});
	}
}
