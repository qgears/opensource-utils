package hu.qgears.parser;

import java.util.List;

import hu.qgears.parser.language.impl.Term;

/**
 * Tree element in the parse result.
 * @author rizsi
 *
 */
public interface ITreeElem {
	public List<? extends ITreeElem> getSubs();

	public String getTypeName();

	public String getString();
	
	int getTextIndexFrom();
	
	int getTextIndexTo();
	
	/**
	 * API to connect user defined objects to this element.
	 * @param key
	 * @param o
	 */
	void setUserObject(String key, Object o);
	
	/**
	 * API to connect user defined objects to this element.
	 * @param key
	 * @return
	 */
	Object getUserObject(String key);

	/**
	 * Get the type of this parsed element.
	 * @return
	 */
	Term getType();
}
