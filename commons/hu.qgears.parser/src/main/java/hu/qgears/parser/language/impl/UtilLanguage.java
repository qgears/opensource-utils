package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import hu.qgears.commons.MultiMapTreeImpl;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.GenerationRules;
import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;
import hu.qgears.parser.tokenizer.impl.TextSource;


public class UtilLanguage {
	/**
	 * Check the integrity of the parsed language.
	 * 
	 * @param language
	 */
	public static void checkLanguage(ILanguage language) {
		assertTrue(language.getRootTerm()!=null);
		assertTrue(language.getTermFilterDef().getRemainingTerms()
				.contains(language.getRootTerm().getName()));
		for (Term term : language.getTerms()) {
			if (!EType.token.equals(term.getType())) {
				assertFalse("Filtered out non-terminal: "+term.getName(),
								language.getTokenFilterDef().getToFilter().contains(
										term.getName()));
			}
		}
	}

	private static void assertFalse(String message, boolean b) {
		if(b)
		{
			throw new RuntimeException(message);
		}
	}

	private static void assertTrue(boolean b) {
		if(!b)
		{
			throw new RuntimeException();
		}
	}

	/**
	 * Simplify the rules of the language:
	 *  * multiple binary AND's are converted to a single multiple AND
	 *  * multiple binary OR's are converted to a single multiple OR
	 *  * simple reference that is not marked to be kept by name are replaced by the rule that is referenced by this reference.
	 * @param source
	 * @throws LanguageParseException
	 */
	public static void simplifyLanguage(ILanguage source) throws LanguageParseException {
		Term[] terms = source.getTerms();
		List<Term> lTerms=new ArrayList<Term>();
		TreeMap<String, Term> termMap = new TreeMap<String, Term>();
		for (Term t : terms) {
			termMap.put(t.getName(), t);
		}
		for (Term t : terms) {
			simplifyTerm(termMap, t);
			lTerms.add(t);
		}
		InitNumericIds.initNumericIds(source, lTerms);
	}

	private static void simplifyTerm(TreeMap<String, Term> termMap, Term t) {
		switch (t.getType()) {
		case reference:
		{
			TermRef tr=(TermRef) t;
			List<String> newTerms = new ArrayList<String>();
			simplifyReferenced(termMap, newTerms, tr.getSub(), EType.reference);
			t.subsStr=newTerms;
			break;
		}
		case and:
		{
			TermCompound comp = (TermCompound) t;
			List<String> newTerms = new ArrayList<String>();
			for (Term ts : comp.getSubs()) {
				simplifyReferenced(termMap, newTerms, ts, EType.and);
			}
			comp.subsStr = newTerms;
			break;
		}
		case or:
		{
			TermCompound comp = (TermCompound) t;
			List<String> newTerms = new ArrayList<String>();
			for (Term ts : comp.getSubs()) {
				simplifyReferenced(termMap, newTerms, ts, EType.or);
			}
			comp.subsStr = newTerms;
			break;
		}
		default:
		}
	}

	private static void simplifyReferenced(TreeMap<String, Term> termMap, List<String> ret,Term ts, EType parentType) {
		if (EType.reference.equals(ts.getType()) && ts.isFiltered()) {
			TermRef r=(TermRef) ts;
			Term t=termMap.get(r.getReferenced());
			// System.out.println("Skipped: "+ts.getName()+" "+ts);
			simplifyReferenced(termMap, ret, t, parentType);
		} else if(parentType.equals(ts.getType())&&ts.isFiltered())
		{
			// System.out.println("Skipped: "+ts.getName()+" "+ts);
			for(String ref: ts.subsStr)
			{
				Term t=termMap.get(ref);
				simplifyReferenced(termMap, ret, t, parentType);
			}
		}else
		{
			if(EType.and.equals(parentType) && EType.epsilon.equals(ts.getType()) && ts.isFiltered())
			{
				// Do not add epsilon rules to and lists
			}else
			{
				ret.add(ts.getName());
			}
		}
	}

	/**
	 * TODO term accelerator feature is not ready.
	 * @param lang
	 */
	public static void setupTermAccelerator(ILanguage lang) {
		ElemBuffer b=new ElemBuffer();
		TextSource ts=new TextSource("");
		TokenArray arr=new TokenArray(ts, lang);
		for(Term t: lang.getTerms())
		{
			int processedUntil=0;
			if("doc".equals(t.getName()))
			{
				System.out.println("my conditional breakpoint");
			}
			Set<TermToken> collectNontermOutput=new HashSet<>();
			b.reInit(lang.getTerms(), arr, lang);
			System.out.println("Term: "+t.getType()+" "+ t.getName());
			GenerationRules.generateNonTermForAccelerator(b, t, 0, 0, collectNontermOutput);
//			System.out.println("Buffer0: "+b.print());
			while(b.getCurrentGroupEnd()>processedUntil)
			{
				GenerationRules.generateOnSameGroupForAccelerator(processedUntil, 0, b, collectNontermOutput);
				processedUntil++;
			}
			MultiMapTreeImpl<Integer, TermToken> byId=new MultiMapTreeImpl<>();
			for(TermToken tt: collectNontermOutput)
			{
				byId.putSingle(tt.getTokenType().getId(), tt);
			}
			for(Integer i: byId.keySet())
			{
				TermToken nullFilterToken=null;
				for(TermToken tt: byId.get(i))
				{
					if(tt.getMatchingValue()==null)
					{
						nullFilterToken=tt;
					}
				}
				if(nullFilterToken!=null)
				{
					List<TermToken> tts=byId.get(i);
					tts.clear();
					tts.add(nullFilterToken);
				}
			}
//			System.out.println("Buffer1: "+b.print());
			System.out.println("Possible terms: "+byId);
			System.out.println();
//			processedUntil = 0;
//			while(b.getCurrentGroupEnd()>processedUntil)
//			{
//				GenerationRules.generateOnNextGroup(b, processedUntil, 1, null);
//				processedUntil++;
//			}
//			System.out.println(b.print());
		}
	}
}
