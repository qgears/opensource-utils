package hu.qgears.nativeloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * 
 * Utility for checking that all natives is packaged into binary, which is 
 * declared by natives-def.xml.
 * @author agostoni
 *
 */
public class XMLNativeLoaderValidator {

	/**
	 * Checks whether the native binaries, belonging to the specified native 
	 * loader, exist. 
	 * @param loaderInstance the loader, the existence of the native libraries of which
	 * 		are to be checked
	 * @throws Exception either if the checking fails for any reason or there
	 * 		are at least one binary in the XML that does not exist
	 */
	public static void check(XmlNativeLoader loaderInstance) throws Exception {
		
		final XmlHandler handler = new XmlHandler(loaderInstance);
		Class<? extends XmlNativeLoader> loader = loaderInstance.getClass();
		try (final InputStream istream = loader.getResourceAsStream(
				XmlNativeLoader.NATIVES_DEF)) {
			UtilNativeLoader.createSAXParser().parse(istream, handler);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new NativeLoadException(e);
		}
		List<String> missing = new ArrayList<String>(); 
		for (NativeBinary b : handler.getNatives()) {
			String f = b.getFileName();
			if (!b.exists(loaderInstance)) {
				missing.add(f);
			}
		}
		for (NativePreload b : handler.getPreloads()) {
			String packagedPreloadResouce = b.getResource();
			if (!b.exists(loaderInstance)) {
				missing.add(packagedPreloadResouce);
			}
		}
		if (!missing.isEmpty()) {
			throw new Exception("Missing binaries specified by "
					+loader.getSimpleName() + " : "+missing);
		}
	}

}
