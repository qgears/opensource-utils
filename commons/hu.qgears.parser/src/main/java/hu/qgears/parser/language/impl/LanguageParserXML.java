package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.tokenizer.ITokenFilterDef;
import hu.qgears.parser.tokenizer.ITokenizerDef;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;
import hu.qgears.parser.tokenizer.impl.TokenFilterParser;
import hu.qgears.parser.tokenizer.impl.TokenizerParser;
import hu.qgears.parser.util.UtilXml;



/**
 * Parse a language from an XML language description file.
 * 
 * @author rizsi
 * 
 */
public class LanguageParserXML extends AbstractLanguageParser {
	public static final String uniqueDiv = "$";
	Document doc;
	Element langDef;

	public LanguageParserXML(Document doc) throws XPathExpressionException {
		super();
		this.doc = doc;
		langDef = UtilXml
				.selectSingleNode(doc.getDocumentElement(), "parser");
	}

	public static ILanguage parseLanguage(Document doc) throws Exception {
		LanguageParserXML lp = new LanguageParserXML(doc);
		lp.buildLanguage();
		return lp.getLanguage();
	}

	class TermCreator {
		List<Term> terms = new ArrayList<Term>();

		List<Term> createTerms(Element parent, String namePref)
				throws LanguageParseException, XPathExpressionException {
			List<Term> ret = new ArrayList<Term>();
			int ctr = 0;
			for (Element n : UtilXml.selectNodes(parent, "term")) {
				Term t = createTerm(n, namePref + uniqueDiv + (ctr++));
				if (t == null)
					throw new RuntimeException();
				ret.add(t);
			}
			return ret;
		}

		Term createCompoundTerm(Element tDef, String name, String type)
				throws LanguageParseException, XPathExpressionException {
			TermCompound ret = null;
			List<Term> subs = createTerms(tDef, name);
			if ("+".equals(type)) {
				ret = new TermAnd(name);
				;
			}
			if ("|".equals(type)) {
				ret = new TermOr(name);
				;
			}
			for (Term sub : subs) {
				ret.getSubsStr().add(sub.getName());
			}
			return ret;
		}

		Term createTerm(Element _tDef, String name)
				throws LanguageParseException, XPathExpressionException {
			Element tDef = (Element) _tDef;
			String type = tDef.getAttribute("t");
			Term ret;
			if ("E".equals(type)) {
				ret = new TermEpsilon(name);
			} else if ("d".equals(type)) {
				ret = new TermRef(name, UtilXml.getText(tDef));
			} else if ("*".equals(type)) {
				ret = new TermZeroOrMore(name, UtilXml.getText(tDef));
			} else if ("*1".equals(type)) {
				ret = new TermOneOrMore(name, UtilXml.getText(tDef));
			} else if ("+".equals(type) || "|".equals(type)) {
				ret = createCompoundTerm(tDef, name, type);
			} else {
				ret = null;
			}
			if (ret != null)
				terms.add(ret);
			return ret;
		}

	}

	@Override
	protected Set<String> parseRemainingTerms() throws XPathExpressionException {
		Set<String> ret = new HashSet<String>();
		Element termFiltDef = UtilXml.selectSingleNode(doc
				.getDocumentElement(), "termFilter");
		for (Element n : UtilXml.selectNodes(termFiltDef, "accept")) {
			ret.add(UtilXml.getText(n));
		}
		return ret;
	}

	@Override
	protected String parseRootName() throws XPathExpressionException {
		String rootName = UtilXml.selectSingleNodeText(langDef, "root");
		return rootName;
	}

	@Override
	protected List<Term> parseTerms() throws LanguageParseException,
			XPathExpressionException {
		List<Term> ret = new ArrayList<Term>();
		for (Element n : UtilXml.selectNodes(langDef, "def")) {
			String name = UtilXml.selectSingleNodeText(n, "name");
			Element term = UtilXml.selectSingleNode(n, "term");
			TermCreator tc = new TermCreator();
			tc.createTerm(term, name);
			ret.addAll(tc.terms);
		}
		return ret;
	}

	@Override
	protected ITokenFilterDef parseTokenFilter() throws LanguageParseException,
			XPathExpressionException {
		Element tokFDef = UtilXml.selectSingleNode(doc.getDocumentElement(),
				"tokenFilter");

		ITokenFilterDef tfd = new TokenFilterParser().parse(tokFDef);
		return tfd;
	}

	@Override
	protected ITokenizerDef parseTokenizer() throws TokenizerException,
			LanguageParseException, XPathExpressionException {
		Element tokDef = UtilXml.selectSingleNode(doc.getDocumentElement(),
				"tokenizer");
		ITokenizerDef td = new TokenizerParser().parse(tokDef);
		return td;
	}
}
