package hu.qgears.parser;

import java.util.Map;
import java.util.TreeMap;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenRecognizerFactory;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.recognizer.factories.Fact;



public class TokenizerImplManager {
	Map<String, ITokenRecognizerFactory> facts = new TreeMap<String, ITokenRecognizerFactory>();

	public ITokenRecognizer getRecognizer(String id, ITokenType type,
			String config) throws TokenizerException {
		ITokenRecognizerFactory fact = facts.get(id);
		if (fact == null)
			return null;
		return fact.create(type, id, config);
	}

	public TokenizerImplManager() {
		super();
		addFact(new Fact());
	}

	public void addFact(ITokenRecognizerFactory fact) {
		for (String s : fact.getIds()) {
			facts.put(s, fact);
		}
	}
}
