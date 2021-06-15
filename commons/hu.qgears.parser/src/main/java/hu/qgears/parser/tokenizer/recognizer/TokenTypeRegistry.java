package hu.qgears.parser.tokenizer.recognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenRecognizerFactory;
import hu.qgears.parser.tokenizer.TokenizerException;

/**
 * Basic implementation of token type registry.
 */
public class TokenTypeRegistry implements ITokenRecognizerFactory {
	public interface Factory
	{
		ITokenRecognizer create(ITokenType type, String id, String config);
	}
	private Map<String, Factory> reg=new HashMap<String, Factory>();
	@Override
	public ITokenRecognizer create(ITokenType type, String id, String config) throws TokenizerException {
		Factory f=reg.get(id);
		if(f!=null)
		{
			return f.create(type, id, config);
		}
		return null;
	}

	@Override
	public List<String> getIds() {
		return new ArrayList<String>(reg.keySet());
	}
	public void register(String id, Factory factory)
	{
		reg.put(id, factory);
	}
}
