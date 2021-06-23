package hu.qgears.parser.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.IDGen;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.impl.TextSource;
import hu.qgears.parser.tokenizer.impl.Tokenizer;
import hu.qgears.parser.tokenizer.impl.TokenizerDef;
import hu.qgears.parser.tokenizer.recognizer.RecognizerConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerDoubleNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWhiteSpace;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWord;
import hu.qgears.parser.util.UtilFile;



public class TestTokenizer {
	@Test
	public void run() throws Exception
	{
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		recogs.add(new RecognizerWhiteSpace(new TokenType("whitespace")));
		recogs.add(new RecognizerId(new TokenType("id")));
		recogs.add(new RecognizerWord(new TokenType("word")));
		recogs.add(new RecognizerNumber(new TokenType("number")));
		recogs.add(new RecognizerConst(new TokenType("="), "="));
		List<ITokenType> types = new ArrayList<ITokenType>();
		for (ITokenRecognizer rec : recogs) {
			types.addAll(rec.getRecognizedTokenTypes());
		}
		new IDGen().genTokenTypeIds(types);
		Tokenizer tok = new Tokenizer(new TokenizerDef(recogs));
		ITextSource ts = new TextSource("alma 2=nincs");
		StringBuilder ret=new StringBuilder();
		List<IToken> toks = tok.tokenize(ts);
		for (IToken t : toks) {
			ret.append(t);
			ret.append("\n");
		}
		Assert.assertEquals(UtilFile.loadFileAsString(getClass().getResource("TestTokenizer-output.txt")), ret.toString());
	}
	@Test
	public void testNumberTokenizer()
	{
		RecognizerDoubleNumber rdn=new RecognizerDoubleNumber(null);
		Assert.assertEquals(3, rdn.getGeneratedToken(new TextSource("112")).getLength());
		Assert.assertEquals(5, rdn.getGeneratedToken(new TextSource(".10e6")).getLength());
	}
}
