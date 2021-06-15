package hu.qgears.parser.tokenizer.recognizer.factories;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenRecognizerFactory;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.recognizer.RecognizerAnyCaseConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerComment;
import hu.qgears.parser.tokenizer.recognizer.RecognizerConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerSComment;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWhiteSpace;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWord;



public class Fact implements ITokenRecognizerFactory {

	public ITokenRecognizer create(ITokenType type, String id, String config)
			throws TokenizerException {
		if ("word".equals(id)) {
			return new RecognizerWord(type);
		}
		if ("number".equals(id)) {
			return new RecognizerNumber(type);
		}
		if ("whitespace".equals(id)) {
			return new RecognizerWhiteSpace(type);
		}
		if ("id".equals(id)) {
			return new RecognizerId(type);
		}
		if ("const".equals(id)) {
			return new RecognizerConst(type, config);
		}
		if ("anyCase".equals(id)) {
			return new RecognizerAnyCaseConst(type, config);
		}
		if ("stringConst".equals(id)) {
			return new RecognizerString(type);
		}
		if ("comment".equals(id)) {
			return new RecognizerComment(type);
		}
		if ("singleLineComment".equals(id)) {
			return new RecognizerSComment(type);
		}
		return null;
	}

	public List<String> getIds() {
		String[] types = { "word", "number", "whitespace", "id", "const", "anyCase",
				"stringConst", "comment", "singleLineComment" };
		List<String> ret = new ArrayList<String>();
		for (String s : types) {
			ret.add(s);
		}
		return ret;
	}

}
