package hu.qgears.parser.tokenizer;

import java.util.Arrays;

import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.tokenizer.impl.TextSource;

/** An array of token data - result of tokenizer. */
final public class TokenArray {
	int n=0;
	int[] data=new int[3*128];
	private TextSource source;
	private ILanguage language;
	
	public TokenArray(TextSource source, ILanguage language) {
		super();
		this.source = source;
		this.language=language;
	}
	public void addToken(int tokenTypeId, int position, int length) {
		ensureCapacity(n+1);
		data[n*3]=tokenTypeId;
		data[n*3+1]=position;
		data[n*3+2]=length;
		n++;
	}
	private void ensureCapacity(int nentry) {
		int nint=nentry*3;
		if(nint>=data.length)
		{
			data=Arrays.copyOf(data, data.length*2);
		}
	}
	public int size() {
		return n;
	}
	public Token remove(int index) {
		Token ret=get(index);
		for(int i=index*3; i<n*3;++i)
		{
			data[i]=data[i+3];
		}
		n-=1;
		return ret;
	}
	public int type(int i) {
		return data[i*3];
	}
	public TextSource getSource() {
		return source;
	}
	/**
	 * 
	 * @deprecated use API that do not instantiate this as an object
	 * @param i
	 * @return
	 */
	@Deprecated
	public Token get(int i) {
		return new Token(language.getTokenizerDef().tokenTypeById(type(i)), source, pos(i), length(i));
	}
	public int pos(int i) {
		return data[i*3+1];
	}
	public int length(int i) {
		return data[i*3+2];
	}
	public ILanguage getLanguage() {
		return language;
	}
	@Deprecated
	public Token[] getAllTokens() {
		Token[] ret=new Token[n];
		for(int i=0;i<n;++i)
		{
			ret[i]=get(i);
		}
		return ret;
	}
}
