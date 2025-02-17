package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.util.ParseRuntimeException;

public class RecognizerAnyCaseConst extends RecognizerAbstract implements
		ITokenRecognizer {
	private String c;
	private char[] arrUpper;
	private char[] arrLower;

	@Override
	public int getGeneratedToken(char[] arr, int at) {
		int max=at+arrUpper.length;
		if(max>arr.length)
		{
			return 0;
		}
		for(int i=0;i<arrUpper.length;++i)
		{
			char c=arr[at+i];
			char u=arrUpper[i];
			char l=arrLower[i];
			if(c!=u && c!=l)
			{
				return 0;
			}
		}
		return arrUpper.length;
	}

	public RecognizerAnyCaseConst(ITokenType tokenType, String c)
			throws ParseRuntimeException {
		super(tokenType);
		if (c.length() < 1)
			throw new ParseRuntimeException("invalid token: = length constant");
		this.c = c;
		arrUpper=c.toUpperCase().toCharArray();
		arrLower=c.toLowerCase().toCharArray();
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
		return c == arrLower[0] || c == arrUpper[0];
	}
}
