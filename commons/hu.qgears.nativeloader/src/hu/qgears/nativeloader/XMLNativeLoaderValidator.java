package hu.qgears.nativeloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * Utility for checking that all natives is packaged into binary, which is declated by natives-def.xml
 * @author agostoni
 *
 */
public class XMLNativeLoaderValidator {

	
	public static void check(Class<? extends XmlNativeLoader> loader) throws Exception {
		
		XmlHandler handler = new XmlHandler();
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			InputStream istream = loader.getResourceAsStream(XmlNativeLoader.NATIVES_DEF);
			InputSource isource = new InputSource(istream);
			reader.setContentHandler(handler);
			reader.parse(isource);
		} catch (SAXException e) {
			throw new NativeLoadException(e);
		} catch (IOException e) {
			throw new NativeLoadException(e);
		}
		List<String> missing = new ArrayList<String>(); 
		for (NativeBinary b : handler.getNatives()) {
			String f = b.getFileName();
			if (!exists(b,loader)) {
				missing.add(f);
			}
		}
		if (!missing.isEmpty()) {
			throw new Exception("Missing binaries specified by "+loader.getSimpleName() + " : "+missing);
		}
//		NativesToLoad ntl = new NativesToLoad(handler.getNatives(), handler.sources);
//		for (SourceFile b : ntl.getBinaries()) {
//			System.out.println( b.getPath());
//		}
		
	}

	private static boolean exists(NativeBinary b, Class<? extends XmlNativeLoader> loader) {
		return b.getUrl(loader) != null;
	}
}
