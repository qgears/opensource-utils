package hu.qgears.tools;

public class PrintAllowedChars {
	public static void main(String[] args) {
		for(char i='a'; i<='z';++i)
		{
			System.out.print(i);
		}
		for(char i='A'; i<='Z';++i)
		{
			System.out.print(i);
		}
		System.out.print("@:/.-");
	}
}
