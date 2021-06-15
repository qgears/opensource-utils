package hu.qgears.parser.language.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.qgears.parser.language.EType;
import hu.qgears.parser.language.ILanguage;

/**
 * Non-terminal symbol of the language.
 * @author rizsi
 *
 */
abstract public class Term {
	int id;
	String name;
	
	List<String> subsStr = new ArrayList<String>();
	public List<String> getSubsStr() {
		return subsStr;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Term(String name) {
		super();
		this.name = name;
	}

	public abstract EType getType();

	@Override
	public String toString() {
		return ": "+getType();
	}

	public void toString(StringWriter wri, String prefix) {
		wri.append(prefix + getName()+": "+ toString());
	}

	ILanguage language;

	public Term setLanguage(ILanguage language) {
		this.language = language;
		return this;
	}

	public ILanguage getLanguage() {
		return language;
	}
	/**
	 * Is this symbol filtered out from the resulting tree?
	 * @return true means this element is not present in the resulting tree.
	 */
	public boolean isFiltered() {
		boolean ret = false;
		if (EType.token.equals(getType())) {
			ret = language.getTokenFilterDef().getToFilter().contains(
					((TermToken) this).getTokenType().getName());
		}
		if (!ret) {
			ret = !language.getTermFilterDef().getRemainingTerms().contains(
					getName());
		}
		return ret;
	}
	abstract public void initialize(Map<String, Term> termMap);
}
