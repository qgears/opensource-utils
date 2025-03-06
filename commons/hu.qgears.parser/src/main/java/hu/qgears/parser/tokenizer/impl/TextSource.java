package hu.qgears.parser.tokenizer.impl;

final public class TextSource {
	// CharSequence seq;
	public char[] array;

	public TextSource(char[] array) {
		super();
		this.array = array;
	}

	public TextSource(CharSequence seq) {
		super();
		initlializeArray(seq);
	}

	public TextSource(String seq) {
		super();
		assert (seq != null);
		initlializeArray(seq);
	}
	
	private void initlializeArray(CharSequence seq)
	{
		array=new char[seq.length()];
		for(int i=0;i<array.length;++i)
		{
			array[i]=seq.charAt(i);
		}
	}

	public String firstChars(int length) {
		return firstChars(0, length);
	}

	public CharSequence getFullSequence() {
		return new String(array);
	}
	
	public static Character getCharAt(int pos, char[] array, int i) {
		int p = pos + i;
		if (p >= array.length || p < 0) {
			return null;
		}
		return array[pos + i];
	}


	/**
	 * Copy operation: should not be used in the hot loop
	 * @param from
	 * @param length
	 * @return
	 */
	public String firstChars(int from, int length) {
		return new String(array, from, Math.min(array.length - from, length));
	}

	@Override
	public String toString() {
		return firstChars(10) + "...";
	}

/*	public boolean startsWith(int relPos, String s) {
		int ptr=pos+relPos;
		if(pos+s.length()>getLength())
		{
			return false;
		}
		for(int i=0;i<s.length();++i)
		{
			char c=seq.charAt(ptr);
			if(!(c==s.charAt(i)))
			{
				return false;
			}
			ptr++;
		}
		return true;
	}
	*/
	
	public static boolean startsWith(char[] array, int ptr, char[] s) {
		if(ptr+s.length>array.length)
		{
			return false;
		}
		for(int i=0;i<s.length;++i)
		{
			char c=array[ptr];
			if(!(c==s[i]))
			{
				return false;
			}
			ptr++;
		}
		return true;
	}

	public int getLength() {
		return array.length;
	}

	/**
	 * Copy operation: should not be used in hot loop
	 * @param from
	 * @param length
	 * @return
	 */
	public String lastChars(int from, int length) {
		if (from < length) {
			length = from;
		}
		return firstChars(from - length, length);
	}
	
	/**
	 * Copy operation: should not be used in the hot loop
	 * @param from
	 * @param to
	 * @return
	 */
	public CharSequence substring(int from, int to) {
		if (from < 0) {
			from = 0;
		}
		return firstChars(from, to - from);
	}
}
