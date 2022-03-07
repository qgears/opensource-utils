package hu.qgears.parser.tokenizer.recognizer.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenRecognizerFactory;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.recognizer.RecognizerAnyCaseConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerCStyleHexa;
import hu.qgears.parser.tokenizer.recognizer.RecognizerComment;
import hu.qgears.parser.tokenizer.recognizer.RecognizerConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerDoubleNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerSComment;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWhiteSpace;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWord;
import hu.qgears.parser.tokenizer.recognizer.RecognizerXtextId;
import hu.qgears.parser.tokenizer.recognizer.TokenTypeRegistry;
import hu.qgears.parser.tokenizer.recognizer.TokenTypeRegistry.Factory;


/**
 * The default token recognizers provided by qparser.
 */
public class Fact implements ITokenRecognizerFactory {
	private Map<String, TokenTypeRegistry.Factory> types=new TreeMap<>();
	public Fact()
	{
		types.put("floatingPointNumber", (type, id, config)->new RecognizerDoubleNumber(type));
		types.put("stringConst2", (type, id, config)->new RecognizerString(type, '\''));

		types.put("word", (type, id, config)->new RecognizerWord(type));
		types.put("wholeWord", (type, id, config)->new RecognizerConst(type, config, true));
	
		types.put("cStyleHexa", (type, id, config)->new RecognizerCStyleHexa(type));
		types.put("number", (type, id, config)->new RecognizerNumber(type));
		types.put("whitespace", (type, id, config)->new RecognizerWhiteSpace(type));
		types.put("id", (type, id, config)->new RecognizerId(type));
		types.put("xtextId", (type, id, config)->new RecognizerXtextId(type));
		types.put("const", (type, id, config)->new RecognizerConst(type, config, false));
		types.put("anyCase", (type, id, config)->new RecognizerAnyCaseConst(type, config));
		types.put("stringConst", (type, id, config)->new RecognizerString(type, '\"'));
		types.put("comment", (type, id, config)->new RecognizerComment(type));
		types.put("comment2", (type, id, config)->new RecognizerComment(type, "/-", "-/"));
		types.put("singleLineComment", (type, id, config)->new RecognizerSComment(type));
}

	public ITokenRecognizer create(ITokenType type, String id, String config)
			throws TokenizerException {
		Factory f=types.get(id);
		return f.create(type, id, config);
	}

	public List<String> getIds() {
//		String[] types = { "word", "number", "whitespace", "id", "const", "anyCase",
//				"stringConst", "comment", "singleLineComment" };
//		List<String> ret = new ArrayList<String>();
//		for (String s : types) {
//			ret.add(s);
//		}
		return new ArrayList<>(types.keySet());
	}

}
