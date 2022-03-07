package hu.qgears.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.expression.ExpLang;
import hu.qgears.parser.impl.Parser;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.util.ParseRuntimeException;



/**
 * Helper methods for the generic parser.
 * 
 * @author rizsi
 * 
 */
public class LanguageHelper {
	public static String addExpressionLanguage(String language,
			String expressions) throws IOException {
		ExpLang el = ExpLang.parse(expressions);
		String expLang = el.renderExpLang();
		String lang = language.replaceAll(el.insertHere, expLang);
		return lang;
	}

	/**
	 * Create a parser instance for a language, an input text and a parse error
	 * logger. The returned parser is for single use! 
	 * @param lang
	 * @param text
	 * @param logger
	 * @return
	 * @throws Exception
	 */
	public static IParser createParser(ILanguage lang,
		String text,
		ParserLogger logger) throws Exception {
		return new Parser(lang, text, logger);
	}


	public static void print(ITreeElem tree) {
		System.out.println(new TreeRenderer().render2(tree, ""));
	}
	/**
	 * Find child element of tree node
	 * by its type.
	 * Not existing or multiple finds throw Exception!
	 * @param type
	 * @return
	 * @throws ParseRuntimeException in case number of children with type is not exactly 1
	 */
	public static ITreeElem singleChildByType(ITreeElem t, String type)
	{
		ITreeElem ret=null;
		for(ITreeElem c: t.getSubs())
		{
			if(type.equals(c.getTypeName()))
			{
				if(ret!=null)
				{
					throw new ParseRuntimeException(t, "Node has nore than 1 child of type: '"+type+"'");
				}
				ret=c;
			}
		}
		if(ret==null)
		{
			throw new ParseRuntimeException(t, "Node has no child of type: '"+type+"'");
		}
		return ret;
	}
	/**
	 * Find child element of tree node
	 * by its type.
	 * Not existing child returns null. Multiple finds throw Exception!
	 * @param type
	 * @return
	 * @throws ParseRuntimeException in case number of children with type is not exactly 1
	 */
	public static ITreeElem childByType(ITreeElem t, String type)
	{
		ITreeElem ret=null;
		for(ITreeElem c: t.getSubs())
		{
			if(type.equals(c.getTypeName()))
			{
				if(ret!=null)
				{
					throw new ParseRuntimeException(t, "Node has nore than 1 child of type: '"+type+"'");
				}
				ret=c;
			}
		}
		return ret;
	}
	public static List<ITreeElem> getAllByType(ITreeElem t, String type)
	{
		List<ITreeElem> ret=new ArrayList<>();
		for(ITreeElem c: t.getSubs())
		{
			if(type.equals(c.getTypeName()))
			{
				ret.add(c);
			}
		}
		return ret;
	}
	/**
	 * Get the single child of the node.
	 * Assert that the node has exactly one child
	 * @param re
	 * @return
	 * @throws ParseRuntimeException
	 */
	public static ITreeElem singleChild(ITreeElem re) {
		return childByIndexAndAssertSize(re, 0, 1);
	}

	public static ITreeElem childByIndexAndAssertSize(ITreeElem t, int i, int j) {
		if(t.getSubs().size()!=j)
		{
			LanguageHelper.print(t);
			throw new ParseRuntimeException(t, "Number of children is not "+j+" but "+t.getSubs().size()+".");
		}
		return t.getSubs().get(i);
	}
}
