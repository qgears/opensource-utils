package hu.qgears.parser.tokenizer.impl;

final public class TextSource {
	CharSequence seq;
	public char[] array;
	int pos;

	public TextSource(CharSequence seq, int pos, char[] array) {
		super();
		this.seq = seq;
		this.pos = pos;
		this.array = array;
	}
	public TextSource(CharSequence seq, int pos) {
		super();
		this.seq = seq;
		this.pos = pos;
		initlializeArray();
	}

	public TextSource(CharSequence seq) {
		super();
		this.seq = seq;
		initlializeArray();
	}

	public TextSource(String seq) {
		super();
		assert (seq != null);
		this.seq = seq;
		initlializeArray();
	}
	
	private void initlializeArray()
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
		if (index > seq.length())
			index = seq.length();
		return index;
	}

	public String firstChars(int length) {
		return seq.subSequence(pos, normalize(pos + length))
				.toString();
	}

	public CharSequence getCurrentSequence() {
		return seq.subSequence(pos, seq.length());
	}

	public CharSequence getFullSequence() {
		return seq;
	}

	public int getPosition() {
		return pos;
	}

	public boolean isEmpty() {
		return pos == seq.length();
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
		if (p >= seq.length() || p < 0) {
			return null;
		}
		return seq.charAt(pos + i);
	}

	public TextSource getClone() {
		return new TextSource(seq, pos, array);
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
		return seq.subSequence(Math.max(0, from-length), from).toString();
	}
	/**
	 * Copy operation: should not be used in the hot loop
	 * @param pos
	 * @param end
	 * @return
	 */
	public CharSequence substring(int pos, int end) {
		return seq.subSequence(pos, end);
	}
}
