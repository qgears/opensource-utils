package hu.qgears.parser.tokenizer.impl;

import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenizerDef;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.TokenizerException;



public class Tokenizer {
	ITokenizerDef tDef;

	// List<ITokenRecognizer> tokenizers=new ArrayList<ITokenRecognizer>();
	public void tokenize(TokenArray ret, TextSource seq, IParserReceiver receiver) throws TokenizerException {
		while (!seq.isEmpty()) {
			boolean couldTokenize = false;
			for (ITokenRecognizer rec : tDef.getRecognizers()) {
				int pass = rec.getGeneratedToken(seq);
				if(pass>0)
				{
					ret.addToken(rec.getRecognizedTokenTypes().getId(), seq.getPosition(), pass);
					seq.pass(pass);
					couldTokenize = true;
					break;
				}
			}
			if (!couldTokenize) {
				TokenizerException exc=new TokenizerException("Cannot tokenize: '"
						+ seq.firstChars(20) + "'", seq.getPosition());
				receiver.tokenizeError(exc);
				return;
			}
		}
		return;
	}

	public Tokenizer(ITokenizerDef tDef) {
		super();
		this.tDef = tDef;
	}
}
