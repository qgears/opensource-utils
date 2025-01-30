package hu.qgears.parser.language;

/**
 * Token type - result of the tokenizer.
 */
public interface ITokenType {
	int getId();
	void setId(int id);
	String getName();
}
