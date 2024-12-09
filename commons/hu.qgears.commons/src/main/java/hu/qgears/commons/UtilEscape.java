package hu.qgears.commons;

public class UtilEscape {

	private UtilEscape() {
		// disable constructor of utility class
	}

	/**
	 * Escape the input to be writable into a Java string.
	 * 
	 * TODO implement properly!
	 * 
	 * @param str
	 * @return
	 */
	public static String escapeToJavaString(String str)
	{
		StringBuilder ret=new StringBuilder();
		for(int i=0;i<str.length();++i)
		{
			char ch=str.charAt(i);
			if(ch=='\n')
			{
				ret.append("\\n");
			}
			else if(ch=='\'')
			{
				ret.append("\\\'");
			}
			else if(ch=='\"')
			{
				ret.append("\\\"");
			}
			else if(ch=='\\')
			{
				ret.append("\\\\");
			}
			else if(ch=='\r')
			{
				ret.append("\\r");
			}else if(ch<' '||ch > 0x7f||ch=='\\')
			{
				ret.append("\\u");
				ret.append(UtilString.padLeft(Integer.toHexString(ch), 4, '0'));
			}
			else
			{
				ret.append(ch);
			}
		}
		return ret.toString();
	}
	
	/**
	 * Escapes string to be writable in an ANSI C char* variable.
	 * 
	 * @param str
	 * @return the string as to be written in a char* variable in C
	 */
	public static String escapeToANSICString(String str)
	{
		StringBuilder ret=new StringBuilder();
		for(int i=0;i<str.length();++i)
		{
			char ch=str.charAt(i);
			if(ch=='\n')
			{
				ret.append("\\n");
			}
			else if(ch=='\'')
			{
				ret.append("\\\'");
			}
			else if(ch=='\"')
			{
				ret.append("\\\"");
			}
			else if(ch=='\\')
			{
				ret.append("\\\\");
			}
			else if(ch=='\r')
			{
				ret.append("\\r");
			}else if (ch=='\t')
			{
				ret.append("\\t");
			}
			else if (ch=='?')
			{
				ret.append("\\?");
			}
			else if (ch=='\f')
			{
				ret.append("\\f");
			}
			else if (ch=='\b')
			{
				ret.append("\\b");
			}
			else if(ch<' '||ch > 0x7f)
			{
				ret.append("\\u");
				ret.append(UtilString.padLeft(Integer.toHexString(ch), 4, '0'));
			}
			else
			{
				ret.append(ch);
			}
		}
		return ret.toString();
	}
}
