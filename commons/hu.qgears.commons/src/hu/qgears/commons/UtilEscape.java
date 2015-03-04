package hu.qgears.commons;

public class UtilEscape {
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
}
