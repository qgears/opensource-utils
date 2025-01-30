package hu.qgears.parser.tokenizer.recognizer;

import java.util.function.Consumer;

import hu.qgears.commons.EscapeString;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.RecognizerAbstract;
import hu.qgears.parser.tokenizer.impl.TextSource;

/**
 * Recognizes string constants.
 * Starts and ends with ".
 * Escaping with \ is allowed: the next character after \ can be " but that does not close the string.
 * 
 * The String representation is intended to follow the Java specification of string constants.
 */
public class RecognizerString extends RecognizerAbstract {
	private char[] endingString;
	private char endingCharacter;
	public RecognizerString(ITokenType tokenType, char endingCharacter) {
		super(tokenType);
		endingString=(""+endingCharacter).toCharArray();
		this.endingCharacter=endingCharacter;
	}
	/**
	 * Remove quotes and unescape the string. Convert the token content to a pure string.
	 * @param s the String in the original representation. Eg: "my \"string\""
	 * @return The string without qoutes and unescaped. eg: my "string"
	 */
	public static String getString(String s) {
		String ret=s.substring(1,s.length()-1);
		return EscapeString.unescapeJava(ret);
	}

	public static String toEscapedConstant(String c) {
		return "\""+EscapeString.escapeJava(c)+"\"";
	}
	@Override
	public int getGeneratedToken(TextSource src) {
		if(src.startsWith(0, endingString))
		{
			int ninside=RecognizerStringInside.getGeneratedToken(src.array, src.getPosition()+1, endingCharacter, '\\');
			if(src.startsWith(1+ninside, endingString))
			{
				return ninside+2;
			}
		}
		return 0;
	}
	@Override
	public void collectPorposals(String tokenTypeName, String prefix, Consumer<String> collector) {
	}
}
