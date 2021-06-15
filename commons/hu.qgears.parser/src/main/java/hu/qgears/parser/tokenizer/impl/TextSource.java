package hu.qgears.parser.tokenizer.impl;

import hu.qgears.parser.tokenizer.ITextSource;

public class TextSource implements ITextSource {
	CharSequence seq;
	int pos;

	public TextSource(CharSequence seq, int pos) {
		super();
		this.seq = seq;
		this.pos = pos;
	}

	public TextSource(CharSequence seq) {
		super();
		this.seq = seq;
	}

	public TextSource(String seq) {
		super();
		assert (seq != null);
		this.seq = seq;
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

	public ITextSource pass(int pass) {
		pos = normalize(pass + pos);
		return this;
	}

	public ITextSource setPosition(int pos) {
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
		return new TextSource(seq, pos);
	}

	public String firstChars(int from, int length) {
		TextSource ch = getClone();
		ch.setPosition(from);
		return ch.firstChars(length);
	}

	@Override
	public String toString() {
		return firstChars(10) + "...";
	}

	@Override
	public boolean startsWith(int relPos, String s) {
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

	@Override
	public int getLength() {
		return seq.length();
	}

	@Override
	public String lastChars(int from, int length) {
		return seq.subSequence(Math.max(0, from-length), from).toString();
	}
}
