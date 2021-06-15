package hu.qgears.parser.language.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.tokenizer.impl.LanguageParseException;


public class InitNumericIds {
	private Language language;
	private List<Term> terms;
	private List<Term> getTerms() {
		return terms;
	}
	private InitNumericIds(ILanguage language, List<Term> terms)
	{
		this.language=(Language)language;
		this.terms=terms;
	}
	public static void initNumericIds(
			ILanguage language,
			List<Term> terms) throws LanguageParseException
	{
		new InitNumericIds(language, terms).doIt();
	}
	private void doIt() throws LanguageParseException {
		createMap();
		initNumericIds();
	}
	private void initNumericIds() throws LanguageParseException {
		{
			Set<String> referenced=new HashSet<String>();
			if(language.getRootName()==null)
			{
				throw new LanguageParseException("Error in language definition: root name is not defined!");
			}
			addReferenced(referenced, language.getRootName());
			addReferenced(referenced, "EOF");
			for(String s:language.getTokenFilterDef().getToFilter())
			{
				addReferenced(referenced, s);
			}
			Set<String> toDrop=getTokensToDrop(referenced);
			int ctr = 0;
			List<Term> termsA = new ArrayList<Term>();
			for (Term t : getTerms()) {
				if(!toDrop.contains(t.getName()))
				{
					((Term) t).setId(ctr);
					termsA.add(t);
					t.initialize(termMap);
					t.setLanguage(language);
					ctr++;
				}
			}
			language.setTerms(termsA.toArray(new Term[0]));
		}
		Term mroot = termMap.get(language.getRootName());
		language.setRootTerm(mroot);
	}

	private Set<String> getTokensToDrop(Set<String> referenced) {
		Set<String> toDrop=new TreeSet<String>();
		for(Term term:getTerms())
		{
			if(!referenced.contains(term.getName()))
			{
				toDrop.add(term.getName());
			}
		}
		return toDrop;
	}
	private void addReferenced(Set<String> referenced,
			String name) throws LanguageParseException {
		if(referenced.add(name))
		{
			Term t=(Term)termMap.get(name);
			if(t==null)
			{
				throw new LanguageParseException("Language error: Term is not defined: "+name);
			}
			for(String s:t.getSubsStr())
			{
				addReferenced(referenced, s);
				
			}
		}
	}

	TreeMap<String, Term> termMap;

	private void createMap() {
		termMap = new TreeMap<String, Term>();
		for (Term t : terms) {
			termMap.put(t.getName(), t);
		}
	}

}
