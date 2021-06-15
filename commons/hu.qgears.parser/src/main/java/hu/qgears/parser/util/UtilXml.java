package hu.qgears.parser.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UtilXml {

	public static List<Element> selectNodes(Element tokDef, String string)
			throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(string);
		Object result = expr.evaluate(tokDef, XPathConstants.NODESET);
		NodeList l = (NodeList) result;
		List<Element> ret = new ArrayList<Element>();
		for (int i = 0; i < l.getLength(); ++i) {
			Node n = l.item(i);
			if (n instanceof Element) {
				ret.add((Element) n);
			}
		}
		return ret;
	}

	public static List<String> selectNodesText(Element tokDef, String string)
			throws XPathExpressionException {
		List<String> ret = new ArrayList<String>();
		for (Element node : selectNodes(tokDef, string)) {
			ret.add(getText(node));
		}
		return ret;
	}

	public static Element selectSingleNode(Element n, String string)
			throws XPathExpressionException {
		List<Element> ret = selectNodes(n, string);
		if (ret.size() > 0) {
			return ret.get(0);
		} else {
			return null;
		}
	}

	public static String selectSingleNodeText(Element n, String string)
			throws XPathExpressionException {
		Element node = selectSingleNode(n, string);
		if (node == null) {
			return null;
		} else {
			return getText(node);
		}
	}

	public static String getText(Element def) {
		return def.getTextContent();
	}

	public static Document loadDocument(URL resource) throws IOException,
			SAXException, ParserConfigurationException {
		InputStream is = resource.openStream();
		try {
			Document ret = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(is);
			return ret;
		} finally {
			is.close();
		}
	}

//	public static Document createDocument() {
//		// TODO Not implemented
//		throw new RuntimeException("Not implemented");
//	}

//	public static Element addElement(Document c, String string) {
//		// TODO Not implemented
//		throw new RuntimeException("Not implemented");
//	}

//	public static void setText(Element ee, String unescape) {
//		// TODO Not implemented
//		throw new RuntimeException("Not implemented");
//	}

}
