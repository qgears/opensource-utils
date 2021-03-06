package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenFilterDef;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenizerDef;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;



public abstract class AbstractLanguageParser {
	private ITokenizerDef td;
	private Language language;

	public Language getLanguage() {
		return language;
	}

	private ITokenFilterDef tfd;

	abstract protected ITokenizerDef parseTokenizer()
			throws TokenizerException, LanguageParseException,
			XPathExpressionException;

	abstract protected ITokenFilterDef parseTokenFilter()
			throws LanguageParseException, XPathExpressionException;

	abstract protected String parseRootName() throws XPathExpressionException;

	abstract protected Set<String> parseRemainingTerms()
			throws XPathExpressionException;

	abstract protected List<Term> parseTerms() throws LanguageParseException,
			XPathExpressionException;

	protected void buildLanguage() throws LanguageParseException {
		try {
			language = new Language();
			td = parseTokenizer();
			language.setTokenizerDef(td);
			tfd = parseTokenFilter();
			language.setTokenFilterDef(tfd);

			List<Term> terms=addTerminals();
			String rootName = parseRootName();
			Set<String> remainingTerms=parseRemainingTerms();
			language.getTermFilterDef().getRemainingTerms().addAll(
					remainingTerms);
			terms.addAll(parseTerms());
			language.setRootName(rootName);
			InitNumericIds.initNumericIds(language, terms);
			new IDGen().genTokenTypeIdsFromRecog(language.getTokenizerDef()
					.getRecognizers());
		} catch (LanguageParseException e) {
			throw e;
		} catch (Exception e) {
			throw new LanguageParseException(e);
		}
	}

	private List<Term> addTerminals() {
		List<Term> terms=new ArrayList<Term>();
		for (ITokenRecognizer recog : td.getRecognizers()) {
			for (ITokenType type : recog.getRecognizedTokenTypes()) {
				terms.add(new TermToken(type.getName(), type, null));
			}
		}
		return terms;
	}

}
