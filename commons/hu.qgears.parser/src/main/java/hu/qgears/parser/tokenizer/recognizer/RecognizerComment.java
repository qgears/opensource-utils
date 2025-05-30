package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerComment extends RecognizerAbstract {
	private char exit0;
	private char exit1;
	char[] open;
	char[] close;
	private int recognizeInside(char[] data, int at) {
			int ctr = 0;
			int maxpos=data.length-1;
			while (at<maxpos) {
				char ch = data[at];
				if (ch == exit0) {
					char chh = data[at+1];
					if (chh == exit1) {
						break;
					}
				}
				ctr++;
				at++;
			}
			return ctr;
		}

	@Override
	public int getGeneratedToken(char[] arr, int at) {
		if(TextSource.startsWith(arr, at, open))
		{
			int ninside=recognizeInside(arr, at+open.length);
			if(TextSource.startsWith(arr, at+open.length+ninside, close))
			{
				int l=open.length+ninside+close.length;
				return l;
			}
		}
		return 0;
	}

	public RecognizerComment(ITokenType tokenType, String open, String close) {
		super(tokenType);
		if(close.length()!=2)
		{
			throw new IllegalArgumentException("commend close string must have length of 2: '"+close+"'");
		}
		this.open=open.toCharArray();
		this.close=close.toCharArray();
		exit0=close.charAt(0);
		exit1=close.charAt(1);
	}
	public RecognizerComment(ITokenType tokenType) {
		this(tokenType, "/*", "*/");
	}

	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}
	
	@Override
	public boolean tokenCanStartWith(char c) {
		return c==open[0];
	}
}
