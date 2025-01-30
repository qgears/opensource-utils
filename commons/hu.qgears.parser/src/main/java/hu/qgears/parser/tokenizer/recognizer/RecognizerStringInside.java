package hu.qgears.parser.tokenizer.recognizer;

public class RecognizerStringInside {
	public static int getGeneratedToken(char arr[], int at, char stringEndingCharacter, char escapeCharacter) {
		int ctr = 0;
		boolean lastEscape = false;
		for(;at<arr.length;++at)
		{
			char ch=arr[at];
			if(!lastEscape && ch==stringEndingCharacter)
			{
				return ctr;
			}
			if(lastEscape)
			{
				lastEscape=false;
			}
			else
			{
				lastEscape=ch==escapeCharacter;
			}
			ctr++;
		}
		return 0;
	}
}
