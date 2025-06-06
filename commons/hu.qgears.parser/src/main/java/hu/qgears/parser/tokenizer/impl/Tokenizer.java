package hu.qgears.parser.tokenizer.impl;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.TokenizerException;

public class Tokenizer {
	TokenizerDef tDef;

	public void tokenize(TokenArray ret, TextSource seq, IParserReceiver receiver) throws TokenizerException {
		char[] array = seq.array;
		int at = 0;
		while (at < array.length) {
			boolean couldTokenize = false;
			char c = array[at];
			for (ITokenRecognizer rec : tDef.getRecognizers(c)) {
				int pass = rec.getGeneratedToken(array, at);
				if(pass>0)
				{
					ret.addToken(rec.getRecognizedTokenType().getId(), at, pass);
					at += pass;
					couldTokenize = true;
					break;
				}
			}
			if (!couldTokenize) {
				TokenizerException exc=new TokenizerException("Cannot tokenize: '"
						+ seq.firstChars(at, 20) + "'", at);
				receiver.tokenizeError(exc);
				return;
			}
		}
		// Add EOF token to the token list
		ret.addToken(ret.getLanguage().getTokenizerDef().getEof().getId(), at, 0);
		return;
	}

	public Tokenizer(TokenizerDef tDef) {
		super();
		this.tDef = tDef;
	}
}
