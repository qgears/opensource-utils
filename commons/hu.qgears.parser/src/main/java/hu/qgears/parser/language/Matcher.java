package hu.qgears.parser.language;

public class Matcher {
	private boolean caseSensitive;
	private String value;
	public Matcher(boolean caseSensitive, String value) {
		super();
		this.caseSensitive = caseSensitive;
		this.value = value;
	}
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
}
