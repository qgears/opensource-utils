package hu.qgears.parser.tokenizer.recognizer;

import hu.qgears.commons.EscapeString;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;

/**
 * Recognizes string constants.
 * Starts and ends with ".
 * Escaping with \ is allowed: the next character after \ can be " but that does not close the string.
 * 
 * The String representation is intended to follow the Java specification of string constants.
 */
public class RecognizerString extends RecognizerConcat {
	@Override
	public IToken getGeneratedToken(ITextSource _src) {
		return super.getGeneratedToken(_src);
	}

	public RecognizerString(ITokenType tokenType, char endingCharacter) {
		super(tokenType);
		addSubToken(new RecognizerConst(new TokenType("dummy"), ""+endingCharacter), true);
		addSubToken(new RecognizerStringInside(new TokenType("dummy"), endingCharacter), false);
		addSubToken(new RecognizerConst(new TokenType("dummy"), ""+endingCharacter), true);
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
}
