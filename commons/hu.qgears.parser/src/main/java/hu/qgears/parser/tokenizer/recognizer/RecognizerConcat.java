package hu.qgears.parser.tokenizer.recognizer;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.SimpleToken;

public class RecognizerConcat extends RecognizerAbstract implements
		ITokenRecognizer {
	public RecognizerConcat(ITokenType tokenType) {
		super(tokenType);
	}

	Config config = new Config();

	public static class Config {
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		List<Boolean> must = new ArrayList<Boolean>();
	}

	public void addSubToken(ITokenRecognizer recog, boolean must) {
		config.must.add(must);
		config.recogs.add(recog);
	}

	@Override
	public IToken getGeneratedToken(ITextSource _src) {
		ITextSource src = _src.getClone();
		int ctr = 0;
		for (int i = 0; i < config.recogs.size(); ++i) {
			IToken t;
			if (src.isEmpty())
				t = null;
			else
				t = config.recogs.get(i).getGeneratedToken(src);
			if (t == null && config.must.get(i)) {
				return null;
			}
			if (t != null) {
				ctr += t.getLength();
				src = src.pass(t.getLength());
			}
		}
		if(checkRecognizedToken(_src, ctr))
		{
			return new SimpleToken(getTokenType(), _src, ctr);
		}else
		{
			return null;
		}
	}

	protected boolean checkRecognizedToken(ITextSource _src, int ctr) {
		return true;
	}
}
