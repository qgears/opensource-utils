package hu.qgears.parser.language;

import java.util.List;
import java.util.function.Consumer;

import hu.qgears.parser.tokenizer.TokenArray;

/**
 * Matcher is used to check a possible token match whether it
 * matches a symbol or not.
 * Used in case of grammar symbols with restriction. These grammar symbols
 * match tokens not only by type but also by their content.
 * This is a useful feature thus keywords of a language are not detected while tokenizing.
 * The keywords are not reserved tokens so keywords may be used as simple identifiers.
 */
public class Matcher {
	private boolean caseSensitive;
	private String value;
	private String valueUpperCase;
	private String valueLowerCase;
	public Matcher(boolean caseSensitive, String value) {
		super();
		this.caseSensitive = caseSensitive;
		this.value = value;
		this.valueUpperCase=value.toUpperCase();
		this.valueLowerCase=value.toLowerCase();
	}
	/**
	 * Check if this matcher matches the token content.
	 * @param txt
	 * @return
	 */
	public boolean matches(TokenArray tokens, int tokenIndex) {
		int pos=tokens.pos(tokenIndex);
		int l=tokens.length(tokenIndex);
		if(l!=value.length())
		{
			return false;
		}
		char [] arr=tokens.getSource().array;
		if(caseSensitive)
		{
			for(int i=0;i<l;++i)
			{
				if(arr[pos+i]!=value.charAt(i))
				{
					return false;
				}
			}
			return true;
		}else
		{
			for(int i=0;i<l;++i)
			{
				if(arr[pos+i]!=valueLowerCase.charAt(i) && arr[pos+i]!=valueUpperCase.charAt(i))
				{
					return false;
				}
			}
			return true;
		}
	}
	@Override
	public String toString() {
		return value;
	}
	/**
	 * Add the possible token contents that would match this matcher.
	 * Used by context sensitive recommendation subsystem.
	 * @param collect
	 */
	public void collectPossibleValues(List<String> collect)
	{
		collect.add(value);
	}
	/**
	 * Add the possible token contents that would match this matcher.
	 * Used by context sensitive content assist recommendation subsystem.
	 * @param collect
	 */
	public void collectPossibleValues(Consumer<String> collect)
	{
		collect.accept(value);
	}
}
