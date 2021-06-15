package hu.qgears.parser.tokenizer;

import java.util.List;

import hu.qgears.parser.language.ITokenType;


public interface ITokenizerDef {
	List<ITokenRecognizer> getRecognizers();

	ITokenType getEof();
}
