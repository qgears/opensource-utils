package hu.qgears.parser.tokenizer.recognizer;

public class RecognizerIdStart {
	public static int getGeneratedToken(char[] arr, int at, char escapeChar) {
		if(arr.length>at)
		{
			char ch0=arr[at];
			if(escapeChar==ch0)
			{
				if(at+1<arr.length)
				{
					char c=arr[at+1];
					if(Character.isJavaIdentifierStart(c))
					{
						return 2;
					}
				}
			}else
			{
				if(Character.isJavaIdentifierStart(ch0))
				{
					return 1;
				}
			}
		}
		return 0;
	}
	public static int getGeneratedToken(char[] arr, int at) {
		if(arr.length>at)
		{
			char ch0=arr[at];
			if(Character.isJavaIdentifierStart(ch0))
			{
				return 1;
			}
		}
		return 0;
	}
}
