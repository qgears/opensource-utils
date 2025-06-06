package hu.qgears.parser.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.parser.impl.DefaultReceiver;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.language.impl.IDGen;
import hu.qgears.parser.language.impl.Language;
import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.Token;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.impl.TextSource;
import hu.qgears.parser.tokenizer.impl.Tokenizer;
import hu.qgears.parser.tokenizer.impl.TokenizerDef;
import hu.qgears.parser.tokenizer.recognizer.RecognizerAnyCaseConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerCDecimal;
import hu.qgears.parser.tokenizer.recognizer.RecognizerCStyleHexa;
import hu.qgears.parser.tokenizer.recognizer.RecognizerComment;
import hu.qgears.parser.tokenizer.recognizer.RecognizerConst;
import hu.qgears.parser.tokenizer.recognizer.RecognizerDoubleNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerEOF;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerNumber;
import hu.qgears.parser.tokenizer.recognizer.RecognizerSComment;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWhiteSpace;
import hu.qgears.parser.tokenizer.recognizer.RecognizerWord;
import hu.qgears.parser.util.UtilFile;

public class TestTokenizer {
	@Test
	public void run() throws Exception {
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		recogs.add(new RecognizerWhiteSpace(new TokenType("whitespace")));
		recogs.add(new RecognizerAnyCaseConst(new TokenType("anyCaseConst"), "print"));
		recogs.add(new RecognizerId(new TokenType("id")));
		recogs.add(new RecognizerWord(new TokenType("word")));
		recogs.add(new RecognizerNumber(new TokenType("number")));
		recogs.add(new RecognizerConst(new TokenType("="), "="));
		recogs.add(new RecognizerComment(new TokenType("comment")));
		recogs.add(new RecognizerSComment(new TokenType("singleLineComment")));
		TokenType r = new TokenType("EOF");
		recogs.add(new RecognizerEOF(r));
		recogs.add(new RecognizerString(new TokenType("string"), '"'));
		List<ITokenType> types = new ArrayList<ITokenType>();
		for (ITokenRecognizer rec : recogs) {
			types.add(rec.getRecognizedTokenType());
		}
		Language lang=new Language();
		TokenizerDef td=new TokenizerDef(recogs);
		td.setEof(r);
		lang.setTokenizerDef(td);
		new IDGen().genTokenTypeIdsFromRecog(lang);
		
		Tokenizer tok = new Tokenizer(td);
		TextSource ts = new TextSource("alma 2=nincs print/* comment */\nPrInT// single line comment\n\"string\"PRINT");
		StringBuilder ret = new StringBuilder();
		TokenArray toks=new TokenArray(ts, lang);
		tok.tokenize(toks, ts, new DefaultReceiver());
		for (Token t : toks.getAllTokens()) {
			ret.append(t);
			ret.append("\n");
		}
		Assert.assertEquals(UtilFile.loadFileAsString(getClass().getResource("TestTokenizer-output.txt")),
				ret.toString());
	}

	@Test
	public void testNumberTokenizer() {
		RecognizerDoubleNumber rdn = new RecognizerDoubleNumber(null);
		Assert.assertEquals(3, rdn.getGeneratedToken("112".toCharArray(), 0));
		Assert.assertEquals(5, rdn.getGeneratedToken(".10e6".toCharArray(), 0));
	}

	@Test
	public void testHexaTokenizer() {
		RecognizerCStyleHexa rdn = new RecognizerCStyleHexa(null);
		Assert.assertEquals(5, rdn.getGeneratedToken("0x113".toCharArray(), 0));
		Assert.assertEquals(7, rdn.getGeneratedToken("0X42faB".toCharArray(), 0));
	}

	@Test
	public void testXtextIdTokenizer() {
		RecognizerId rdn = new RecognizerId(null, '^');
		Assert.assertEquals(4, rdn.getGeneratedToken("alma".toCharArray(), 0));
		Assert.assertEquals(5, rdn.getGeneratedToken("korte szolo".toCharArray(), 0));
		Assert.assertEquals(6, rdn.getGeneratedToken("^korte szolo".toCharArray(), 0));
	}
	
	@Test(expected = TokenizerException.class)
	public void testCannotTokenizeException() throws Exception {
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		recogs.add(new RecognizerWhiteSpace(new TokenType("whitespace")));
		recogs.add(new RecognizerId(new TokenType("id")));
		recogs.add(new RecognizerWord(new TokenType("word")));
		recogs.add(new RecognizerNumber(new TokenType("number")));
		recogs.add(new RecognizerConst(new TokenType("="), "ß"));
		List<ITokenType> types = new ArrayList<ITokenType>();
		for (ITokenRecognizer rec : recogs) {
			types.add(rec.getRecognizedTokenType());
		}
		Language lang=new Language();
		TokenizerDef td=new TokenizerDef(recogs);
		lang.setTokenizerDef(td);
		new IDGen().genTokenTypeIdsFromRecog(lang);
		
		Tokenizer tok = new Tokenizer(td);
		TextSource ts = new TextSource("alma 2=nincs");
		TokenArray toks=new TokenArray(ts, lang);
		tok.tokenize(toks, ts, new DefaultReceiver());
	}
	
	@Test
	public void testCDecimal() throws Exception {
		List<ITokenRecognizer> recogs = new ArrayList<ITokenRecognizer>();
		recogs.add(new RecognizerWhiteSpace(new TokenType("whitespace")));
		recogs.add(new RecognizerCDecimal(new TokenType("cdecimal")));
		recogs.add(new RecognizerId(new TokenType("id")));
		TokenType r = new TokenType("EOF");
		recogs.add(new RecognizerEOF(r));
		List<ITokenType> types = new ArrayList<ITokenType>();
		for (ITokenRecognizer rec : recogs) {
			types.add(rec.getRecognizedTokenType());
		}
		Language lang=new Language();
		TokenizerDef td=new TokenizerDef(recogs);
		td.setEof(r);
		lang.setTokenizerDef(td);
		new IDGen().genTokenTypeIdsFromRecog(lang);
		
		Tokenizer tok = new Tokenizer(td);
		TextSource ts = new TextSource("alma 11nincs 10lkiscica 123u 11ualma");
		StringBuilder ret = new StringBuilder();
		TokenArray toks=new TokenArray(ts, lang);
		tok.tokenize(toks, ts, new DefaultReceiver());
		for (Token t : toks.getAllTokens()) {
			ret.append(t);
			ret.append("\n");
		}
		Assert.assertEquals(UtilFile.loadFileAsString(getClass().getResource("TestTokenizerCDecimal-output.txt")),
				ret.toString());
	}
	
}
