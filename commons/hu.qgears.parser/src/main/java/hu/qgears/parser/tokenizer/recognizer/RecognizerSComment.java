package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

public class RecognizerSComment extends RecognizerAbstract {
	public RecognizerSComment(ITokenType tokenType) {
		super(tokenType);
	}

	private char[] prefix="//".toCharArray();
	@Override
	public int getGeneratedToken(TextSource src) {
		if(src.startsWith(0, prefix))
		{
			char[] arr=src.array;
			int at=src.getPosition()+2;
			int ctr=2;
			while(at<arr.length)
			{
				char c=arr[at];
				if(c=='\n')
				{
					break;
				}
				ctr++;
				at++;
			}
			return ctr;
		}
		return 0;
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {	
	}
}
