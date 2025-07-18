package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;

public class RecognizerConst extends RecognizerAbstract implements
		ITokenRecognizer {
	String c;
	char[] carr;
	private boolean wholeWord=false;
	@Override
	public int getGeneratedToken(char[] arr, int at) {
		int max=at+carr.length;
		if(max>arr.length)
		{
			return 0;
		}
		int i=0;
		for(i=0;i<carr.length;++i)
		{
			char c=arr[at+i];
			char u=carr[i];
			if(c!=u)
			{
				return 0;
			}
		}
		if (wholeWord && at+i<arr.length && Character.isJavaIdentifierPart(arr[at+i])) {
			return 0;
		}
		return carr.length;
	}

	public RecognizerConst(ITokenType tokenType, String c) {
		this(tokenType, c, false);
	}
	public RecognizerConst(ITokenType tokenType, String c, boolean wholeWord) {
		super(tokenType);
		this.wholeWord=wholeWord;
		if (c.length() < 1)
			throw new IllegalArgumentException("invalid token: = length constant");
		this.c = c;
		carr=c.toCharArray();
	}

	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
		if(c.startsWith(prefix))
		{
			collector.accept(c);
		}
	}
	
	@Override
	public boolean tokenCanStartWith(char c) {
		return c == carr[0];
	}
}
