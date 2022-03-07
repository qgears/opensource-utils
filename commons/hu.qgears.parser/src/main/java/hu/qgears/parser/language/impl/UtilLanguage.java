package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;


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
			if(EType.and.equals(parentType) && EType.epsilon.equals(ts.getType()))
			{
				// Do not add epsilon rules to and lists
			}else
			{
				ret.add(ts.getName());
			}
		}
	}
}
