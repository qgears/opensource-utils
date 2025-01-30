package hu.qgears.parser.tokenizer.impl;

final public class TextSource {
	// CharSequence seq;
	public char[] array;
	int pos;

	public TextSource(char[] array, int pos) {
		super();
		this.pos = pos;
		this.array = array;
	}
	public TextSource(CharSequence seq, int pos) {
		super();
		this.pos = pos;
		initlializeArray(seq);
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

	private int normalize(int index) {
		if (index < 0)
			index = 0;
		if (index > array.length)
			index = array.length;
		return index;
	}

	public String firstChars(int length) {
		if(pos+length>array.length)
		{
			length=array.length-pos;
		}
		return new String(array, pos, length);
	}

	public CharSequence getCurrentSequence() {
		return new String(array, pos, normalize(array.length));
	}

	public CharSequence getFullSequence() {
		return new String(array);
	}

	public int getPosition() {
		return pos;
	}

	public boolean isEmpty() {
		return pos >= array.length;
	}

	public TextSource pass(int pass) {
		pos = normalize(pass + pos);
		return this;
	}

	public TextSource setPosition(int pos) {
		this.pos = pos;
		return this;
	}

	public Character getCharAt(int i) {
		int p = pos + i;
		if (p >= array.length || p < 0) {
			return null;
		}
		return array[pos + i];
	}

	public TextSource getClone() {
		return new TextSource(array, pos);
	}

	/**
	 * Copy operation: should not be used in the hot loop
	 * @param from
	 * @param length
	 * @return
	 */
	public String firstChars(int from, int length) {
		TextSource ch = getClone();
		ch.setPosition(from);
		return ch.firstChars(length);
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
	public boolean startsWith(int relPos, char[] s) {
		int ptr=pos+relPos;
		if(pos+s.length>getLength())
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
		int l=Math.min(length, from);
		return new String(array, from-l, l);
	}
	/**
	 * Copy operation: should not be used in the hot loop
	 * @param pos
	 * @param end
	 * @return
	 */
	public CharSequence substring(int pos, int end) {
		return new String(array, pos, end-pos);
	}
}
