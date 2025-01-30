package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.recognizer.RecognizerEOF;
import hu.qgears.parser.util.UtilString;



/**
 * Parse tokenizer definition from an AST of a language file.
 */
public class TokenizerParserAST {
	public TokenizerDef parse(TokenizerImplManager man, ITreeElem root) throws LanguageParseException,
			TokenizerException {
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		Set<String> usednames=new HashSet<String>();
		for (ITreeElem e : root.getSubs()) {
			String name = e.getTypeName();
			if ("defToken".equals(name)) {
				String na = e.getSubs().get(0).getString();
				if(usednames.contains(na))
				{
					throw new RuntimeException("Id occures multiple times: "+na);
				}
				usednames.add(na);
				String type = e.getSubs().get(1).getString();
				String conf = null;
				if (e.getSubs().size() > 2) {
					conf = e.getSubs().get(2).getString();
				}
				if (conf != null) {
					conf = UtilString.unescape(conf);
				}
				String recogId = UtilString.unescape(type);
				ITokenRecognizer rec = man.getRecognizer(recogId,
						new TokenType(na), conf);
				if (rec == null)
					throw new LanguageParseException(
							"token recognizer not defined: " + recogId);
				recogs.add(rec);
			}
		}
		TokenType eof = new TokenType("EOF");
		recogs.add(new RecognizerEOF(eof));
		TokenizerDef ret = new TokenizerDef(recogs);
		ret.setEof(eof);
		return ret;
	}
}
