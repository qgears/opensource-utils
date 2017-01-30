package hu.qgears.commons;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Useful static methods for manipulating strings.
 * @author rizsi
 *
 */
public class UtilString {
	private static final char[] hexChars=new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	private UtilString() {
		// disable constructor of utility class
	}
	/**
	 * Pad the input string so that its length is <code>length</code>.
	 * Padding characters are written before the input string
	 * (on the left side of the string). 
	 * If the input is longer that the desired length then nothing is done.
	 * @param src the input string
	 * @param length the desired length of input
	 * @param pad padding character
	 * @return the input string padded. Length is equal to <code>length</code>
	 */
	public static String padLeft(String src, int length, char pad)
	{
		if(src.length()<length)
		{
			StringBuilder ret=new StringBuilder(length);
			for(int i=0;i<length-src.length();++i)
			{
				ret.append(pad);
			}
			ret.append(src);
			return ret.toString();
		}
		else
		{
			return src;
		}
	}
	/**
	 * Using Java's <code>StringTokenizer</code> build a list of string from the tokens
	 * @param str string to be tokenized
	 * @param delimiter the delimiter that is passed to <code>StringTokenizer</code>'s constructor
	 * @return tokens in a list of strings
	 */
	public static List<String> split(String str, String delimiter)
	{
		List<String> ret=new ArrayList<String>();
		StringTokenizer tok=new StringTokenizer(str, delimiter);
		while(tok.hasMoreTokens())
		{
			ret.add(tok.nextToken());
		}
		return ret;
	}
	public static String concat(Collection<String> strs, String pre, String delim,
			String post) {
		StringBuilder ret=new StringBuilder();
		ret.append(pre);
		boolean first=true;
		for(String s: strs)
		{
			if(!first)
			{
				ret.append(delim);
			}
			ret.append(s);
			first=false;
		}
		ret.append(post);
		return ret.toString();
	}

	/**
	 * Concatenate a list of strings into a 
	 * single string
	 * @param list strings in the list will be concatenated
	 * @param delimiter this will be printed between the pieces. Will not be printed before and after them.
	 * @return A single string that contains the input list concatenated with delimiters in between
	 */
	public static String concat(List<String> list, String delimiter) {
		StringBuilder ret=new StringBuilder();
		boolean first=true;
		for(String s:list)
		{
			if(!first)
			{
				ret.append(delimiter);
			}
			ret.append(s);
			first=false;
		}
		return ret.toString();
	}
	/**
	 * Concatenate a list of strings into a 
	 * single string
	 * @param list strings in the list will be concatenated
	 * @param commaProvider this comma provider is used to insert a separator before each element output
	 * @return A single string that contains the input list concatenated with delimiters in between
	 * @since 3.0
	 */
	public static String concat(List<String> list, UtilComma commaProvider) {
		StringBuilder ret=new StringBuilder();
		for(String s:list)
		{
			ret.append(commaProvider.getSeparator());
			ret.append(s);
		}
		return ret.toString();
	}
	/**
	 * Concatenate a list of objects into a 
	 * single string. Objects are represented by their toString method
	 * @param list objects toString() representation in the list will be concatenated
	 * @param delimiter this will be printed between the pieces. Will not be printed before and after them.
	 * @return A single string that contains the input list concatenated with delimiters in between
	 */
	public static String concatGenericList(List<?> list, String delimiter) {
		StringBuilder ret=new StringBuilder();
		boolean first=true;
		for(Object s:list)
		{
			if(!first)
			{
				ret.append(delimiter);
			}
			ret.append(""+s);
			first=false;
		}
		return ret.toString();
	}
	/**
	 * Create list that's length is the required by adding fill to the left of the input string.
	 *
	 * eg: Assert.equals(fillLeft("1", 3, '0'),"001");
	 * 
	 * In case the input is longer than the required length then it is returned intact.
	 * @param string input string
	 * @param targetLength required output length
	 * @param fillChar fill places with these characters
	 * @return
	 */
	public static String fillLeft(String string, int targetLength, char fillChar) {
		if(string.length()==targetLength)
		{
			return string;
		}
		StringBuilder ret=new StringBuilder();
		for(int i=string.length(); i<targetLength;++i)
		{
			ret.append(fillChar);
		}
		ret.append(string);
		return ret.toString();
	}
	/**
	 * Convert the input to a string that's first letter is uppercase
	 * (same letter as in input)
	 * @param name
	 * @return name with first letter uppercase or "UNKNOWN" in case input is null or empty
	 */
	@Deprecated
	public static String firstUpperCase(String name)
	{
		if(name!=null&&name.length()>0)
		{
			String s=name;
			return s.substring(0,1).toUpperCase()+s.substring(1);
		}else
		{
			return "UNKNOWN";
		}
	}
	/**
	 * Convert the input to a string that's first letter is uppercase
	 * (same letter as in input)
	 * @param name
	 * @return name with first letter uppercase or "" in case input is null
	 * @since 3.0
	 */
	public static String firstUpperCaseAllowEmpty(String name)
	{
		if(name!=null&&name.length()>0)
		{
			String s=name;
			return s.substring(0,1).toUpperCase()+s.substring(1);
		}else
		{
			return "";
		}
	}
	/**
	 * Convert the input to a string that's first letter is lowercase
	 * (same letter as in input)
	 * @param name
	 * @return name with first letter lowercase or "unknown" in case input is null
	 */
	public static String firstLowerCase(String name)
	{
		if(name!=null&&name.length()>0)
		{
			String s=name;
			return s.substring(0,1).toLowerCase()+s.substring(1);
		}else
		{
			return "unknown";
		}
	}
	/**
	 * Split a string limited by delimiters.
	 * The returned array contains empty strings too!
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static List<String> split(String str, String[] delimiter)
	{
		List<String> ret=new ArrayList<String>();
		StringBuilder curr=new StringBuilder();
		outer:
		for(int i=0;i<str.length();++i)
		{
			for(String delim: delimiter)
			{
				if(str.startsWith(delim, i))
				{
					ret.add(curr.toString());
					curr=new StringBuilder();
					//modifying i is OK here
					i+=delim.length()-1;//NOSONAR
					continue outer; 
				}
			}
			curr.append(str.charAt(i));
		}
		ret.add(curr.toString());
		return ret;
	}
	/**
	 * Convert bytes to hexadecimal representation.
	 * @param bytes
	 * @return
	 */
	public static String toHex(byte[] bytes) {
		return new BigInteger(1,bytes).toString(16);
	}
	/**
	 * Convert bytes to hexadecimal representation.
	 * Pad left with zeroes if necessary
	 * @param bytes
	 * @return
	 */
	public static String toHexPadZero(byte[] bytes) {
		StringBuilder ret=new StringBuilder(bytes.length*2);
		for(byte b: bytes)
		{
			ret.append(hexChars[(b>>4)&0xF]);
			ret.append(hexChars[b&0xF]);
		}
		return ret.toString();
	}
	public static <T> String concat(List<T> list, UtilComma utilComma, CompatFunction<T, String> nameProvider) {
		StringBuilder ret=new StringBuilder();
		try {
			concat(ret, list, utilComma, nameProvider);
		} catch (IOException e) {
			// Never happens with StringBuilder
		}
		return ret.toString();
	}
	public static <T> void concat(Appendable ret, List<T> list, UtilComma utilComma, CompatFunction<T, String> nameProvider) throws IOException {
		for(T t: list)
		{
			ret.append(utilComma.getSeparator());
			ret.append(nameProvider.apply(t));
		}
		ret.append(utilComma.getPost());
	}
}
