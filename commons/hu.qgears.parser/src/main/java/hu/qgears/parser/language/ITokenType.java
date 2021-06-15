package hu.qgears.parser.language;

/**
 * Token type - result of the tokenizer.
 * @author rizsi
 *
 */
public interface ITokenType {
	int getId();

	void setId(int id);

	String getName();
}
