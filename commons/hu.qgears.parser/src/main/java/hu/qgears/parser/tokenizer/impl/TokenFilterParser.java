package hu.qgears.parser.tokenizer.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import hu.qgears.parser.util.UtilXml;



public class TokenFilterParser {
	public TokenFilterDef parse(Element tokDef) throws LanguageParseException,
			XPathExpressionException {
		List<String> toFilter = new ArrayList<String>();
		for (String id : UtilXml.selectNodesText(tokDef, "delete")) {
			toFilter.add(id);
		}
		return new TokenFilterDef(toFilter);
	}
}
