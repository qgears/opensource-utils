package hu.qgears.commons;

import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * SAX API helper to create safe SAX parser that does not access the Internet while parsing
 * an XML file.
 */
public class UtilSAX {
	static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	static {
		saxParserFactory.setValidating(false);
	}

	public static class DummyEntityResolver implements EntityResolver {

		public InputSource resolveEntity(String publicID, String systemID) throws SAXException {

			return new InputSource(new StringReader(""));
		}
	}

	public static SAXParser createSafeSaxParser() throws SAXException, ParserConfigurationException {
		SAXParser saxParser = saxParserFactory.newSAXParser();
		saxParser.getXMLReader().setEntityResolver(new DummyEntityResolver());
		return saxParser;
	}
}
