package hu.qgears.parser.language;

import java.util.List;
import java.util.function.Consumer;

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
	public Matcher(boolean caseSensitive, String value) {
		super();
		this.caseSensitive = caseSensitive;
		this.value = value;
	}
	/**
	 * Check if this matcher matches the token content.
	 * @param txt
	 * @return
	 */
	public boolean matches(String txt) {
		if(caseSensitive)
		{
			return value.equals(txt);
		}else
		{
			return value.toUpperCase().equals(txt.toUpperCase());
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
