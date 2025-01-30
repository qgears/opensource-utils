package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.TokenizerImplManager;
import hu.qgears.parser.language.ITokenType;
import hu.qgears.parser.tokenizer.ITokenRecognizer;
import hu.qgears.parser.tokenizer.ITokenizerDef;
import hu.qgears.parser.tokenizer.TokenizerException;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;
import hu.qgears.parser.tokenizer.impl.TokenFilterDef;
import hu.qgears.parser.tokenizer.impl.TokenFilterParserAST;
import hu.qgears.parser.tokenizer.impl.TokenizerParserAST;



/**
 * Parse a language definition from the AST of a language file parsed.
 * 
 * @author rizsi
 * 
 */
public class LanguageParserAST extends AbstractLanguageParser {
	ITreeElem root;
	TokenizerImplManager tokenManager;
	private LanguageParserAST(TokenizerImplManager tokenManager, ITreeElem root) {
		super();
		this.root = root;
		this.tokenManager=tokenManager;
	}

	public static Language buildLanguageFromAST(TokenizerImplManager tokenManager, ITreeElem root)
			throws LanguageParseException {
		LanguageParserAST lp = new LanguageParserAST(tokenManager, root);
		lp.buildLanguage();
		return lp.getLanguage();
	}

	@Override
	protected ITokenizerDef parseTokenizer() throws TokenizerException,
			LanguageParseException {
		return new TokenizerParserAST().parse(tokenManager, root);
	}

	// private void parseDefinitions() throws LanguageParseException {
	// for (ITreeElem e : root.getSubs()) {
	// String name = e.getTypeName();
	// if ("defTerm".equals(name)) {
	// String na = e.getSubs().get(0).getString();
	// createTerm(e.getSubs().get(1), na);
	// }
	// if("defAcceptTerm".equals(name))
	// {
	// String na = e.getSubs().get(0).getString();
	// ret.getTermFilterDef().getRemainingTerms().add(na);
	// }
	// }
	// }
	class TermParser {
		List<Term> terms = new ArrayList<Term>();

		private List<Term> createTerms(ITreeElem parent, String namePref)
				throws LanguageParseException {
			List<Term> ret = new ArrayList<Term>();
			int ctr = 0;
			ITreeElem sub1 = parent.getSubs().get(0);

			ITreeElem sub2 = null;
			if (parent.getSubs().size() > 1)
				sub2 = parent.getSubs().get(1);
			{
				Term t = createTerm(sub1, namePref
						+ LanguageParserXML.uniqueDiv + (ctr++));
				if (t == null)
					throw new RuntimeException();
				ret.add(t);
			}
			if (sub2 != null) {
				Term t = createTerm(sub2, namePref
						+ LanguageParserXML.uniqueDiv + (ctr++));
				if (t == null)
					throw new RuntimeException();
				ret.add(t);
			}
			return ret;
		}

		private Term createCompoundTerm(ITreeElem tDef, String name, String type)
				throws LanguageParseException {
			List<Term> subs = createTerms(tDef, name);
			{
				TermCompound ret = null;
				if ("termAdd".equals(type)) {
					ret = new TermAnd(name);
					;
				}
				if ("termOr".equals(type)) {
					ret = new TermOr(name);
					;
				}
				if (ret != null) {
					for (Term sub : subs) {
						ret.getSubsStr().add(sub.getName());
					}
					return ret;
				}
			}
			{
				TermMore ret = null;
				if ("termZeroOrMore".equals(type)) {
					ret = new TermZeroOrMore(name, subs.get(0).getName());
					;
				}
				if ("termOneOrMore".equals(type)) {
					ret = new TermOneOrMore(name, subs.get(0).getName());
					;
				}
				if (ret == null)
					throw new RuntimeException();
				return ret;
			}
		}

		private Term createTerm(ITreeElem tDef, String name)
				throws LanguageParseException {
			String type = tDef.getTypeName();
			Term ret;
			if ("termEps".equals(type)) {
				ret = new TermEpsilon(name);
			} else if ("termRef".equals(type)) {
				String n = tDef.getSubs().get(0).getString();
				ret = new TermRef(name, n);
			} else if ("termTokenWithRestriction".equals(type))
			{
				ITreeElem t0=tDef.getSubs().get(0);
				ITreeElem t1=tDef.getSubs().get(1);
				String typeRef=t0.getString();
				String val=t1.getString();
				ITokenType tt=null;
				ITokenRecognizer recog=null;
				for(ITokenRecognizer tr: getLanguage().getTokenizerDef().getRecognizers())
				{
					ITokenType tti=tr.getRecognizedTokenTypes();
					{
						if(typeRef.equals(tti.getName()))
						{
							tt=tti;
							recog=tr;
						}
					}
				}
				if(tt==null)
				{
					throw new LanguageParseException("Token reference with restriction reference is invalid: "+typeRef);
				}
				String matchingString=val.substring(1, val.length()-1);
				ret=new TermToken(name, tt, recog.createMatcher(matchingString));
			} else if ("termZeroOrMore".equals(type)) {
				ret = createCompoundTerm(tDef, name, type);
			} else if ("termOneOrMore".equals(type)) {
				ret = createCompoundTerm(tDef, name, type);
			} else if ("termAdd".equals(type) || "termOr".equals(type)) {
				ret = createCompoundTerm(tDef, name, type);
//			} else if ("termBracket".equals(type))
//			{
//				String a=tDef.getString();
//				String n = tDef.getSubs().get(0).getString();
//				ret = new TermRef(name, n);
			}
			else {
				throw new LanguageParseException("Unknown expression type: "+type+" of "+tDef.getString()+" "+name);
			}
			if (ret != null) {
				terms.add(ret);
			}
			return ret;
		}

	}

	@Override
	protected List<Term> parseTerms() throws LanguageParseException {
		List<Term> ret = new ArrayList<Term>();
		for (ITreeElem e : root.getSubs()) {
			String name = e.getTypeName();
			if ("defTerm".equals(name)) {
				int zeroIndex=0;
				if("markAcceptTerm".equals(e.getSubs().get(0).getTypeName()))
				{
					zeroIndex=1;
				}
				String na = e.getSubs().get(zeroIndex).getString();
				TermParser tp = new TermParser();
				tp.createTerm(e.getSubs().get(zeroIndex+1), na);
				ret.addAll(tp.terms);
			}
		}
		return ret;
	}

	@Override
	protected TokenFilterDef parseTokenFilter() throws LanguageParseException {
		return new TokenFilterParserAST().parse(root);
	}

	@Override
	protected String parseRootName() {
		String rootName = null;
		for (ITreeElem e : root.getSubs()) {
			String name = e.getTypeName();
			if ("defRoot".equals(name)) {
				rootName = e.getSubs().get(0).getString();
			}
		}
		return rootName;
	}

	@Override
	protected Set<String> parseRemainingTerms() {
		Set<String> ret = new HashSet<String>();
		for (ITreeElem e : root.getSubs()) {
			String name = e.getTypeName();
			if ("defAcceptTerm".equals(name)) {
				String na = e.getSubs().get(0).getString();
				ret.add(na);
			}else if ("defTerm".equals(name)) {
				if("markAcceptTerm".equals(e.getSubs().get(0).getTypeName()))
				{
					String na = e.getSubs().get(1).getString();
					ret.add(na);
				}
			}
		}
		return ret;
	}
}
