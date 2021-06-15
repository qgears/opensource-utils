package hu.qgears.parser.tokenizer;

import java.util.List;

import hu.qgears.parser.language.ITokenType;



public interface ITokenRecognizerFactory {
	ITokenRecognizer create(ITokenType type, String id, String config)
			throws TokenizerException;

	List<String> getIds();
}
