package hu.qgears.parser.tokenizer.recognizer;

public class RecognizerIdInside {

	public static int recognize(char[] arr, int at)
	{
		int ctr=0;
		for(;ctr<arr.length-at;++ctr)
		{
			char ch = arr[at+ctr];
			if(!LetterAcceptorId.accept(ch))
			{
				return ctr;
			}
		}
		return ctr;
	}
}
