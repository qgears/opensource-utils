package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenizerDef;
import hu.qgears.parser.tokenizer.TokenizerException;



public class Tokenizer {
	ITokenizerDef tDef;

	// List<ITokenRecognizer> tokenizers=new ArrayList<ITokenRecognizer>();
	public List<IToken> tokenize(ITextSource seq) throws TokenizerException {
		List<IToken> ret = new ArrayList<IToken>();
		while (!seq.isEmpty()) {
			boolean couldTokenize = false;
			for (ITokenRecognizer rec : tDef.getRecognizers()) {
				IToken tok = rec.getGeneratedToken(seq);
				if (tok != null) {
					ret.add(tok);
					if (tok.getLength() == 0)
						throw new TokenizerException(
								"inernal error: zero length token "
										+ tok.getTokenType().getName() + " : '"
										+ seq.firstChars(10) + "'");
					seq.pass(tok.getLength());
					couldTokenize = true;
					break;
				}
			}
			if (!couldTokenize) {
				throw new TokenizerException("Cannot tokenize: '"
						+ seq.firstChars(20) + "'");
			}
		}
		return ret;
	}

	public Tokenizer(ITokenizerDef tDef) {
		super();
		this.tDef = tDef;
	}
}
