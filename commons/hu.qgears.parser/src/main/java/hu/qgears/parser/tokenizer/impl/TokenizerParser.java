package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.recognizer.RecognizerEOF;
import hu.qgears.parser.util.UtilXml;



/**
 * Parse a tokenizer definition from an XML element that defines a tokenizer.
 */
public class TokenizerParser {
	public TokenizerDef parse(Element tokDef) throws LanguageParseException,
			TokenizerException, XPathExpressionException {
		TokenizerImplManager man = new TokenizerImplManager();
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		for (Element n : UtilXml.selectNodes(tokDef, "token")) {
			String id = UtilXml.selectSingleNodeText(n, "id");
			String recogId = UtilXml.selectSingleNodeText(n, "recognizer");
			String conf = UtilXml.selectSingleNodeText(n, "conf");
			ITokenRecognizer rec = man.getRecognizer(recogId,
					new TokenType(id), conf);
			if (rec == null)
				throw new LanguageParseException(
						"token recognizer not defined: " + recogId);
			recogs.add(rec);
		}
		TokenType eof = new TokenType("EOF");
		recogs.add(new RecognizerEOF(eof));
		TokenizerDef ret = new TokenizerDef(recogs);
		ret.setEof(eof);
		return ret;
	}
}
