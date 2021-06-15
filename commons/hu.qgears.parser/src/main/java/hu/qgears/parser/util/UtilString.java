package hu.qgears.parser.util;

import java.util.List;

public class UtilString {

	public static String concat(String pre, List<String> list, String delim,
			String post) {
		StringBuilder ret = new StringBuilder();
		ret.append(pre);
		boolean first = true;
		for (String s : list) {
			if (!first) {
				ret.append(delim);
			}
			ret.append(s);
			first = false;
		}
		ret.append(post);
		return ret.toString();
	}

	public static String unescape(String str) {
		String withoutquotes=str.substring(1, str.length() - 1);
		StringBuilder ret=new StringBuilder();
		boolean prevBackslash=false;
		for(char ch:withoutquotes.toCharArray())
		{
			if(ch=='\\'&&!prevBackslash)
			{
				prevBackslash=true;
			}else
			{
				ret.append(ch);
				prevBackslash=false;
			}
		}
		return ret.toString();
	}
}
