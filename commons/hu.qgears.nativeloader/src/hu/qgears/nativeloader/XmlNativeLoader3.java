package hu.qgears.nativeloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * NativeLoader which parses an XML file to find the native libraries to load 
 * depending on the OS.
 * 
 * @author rizsi
 * 
 */
public abstract class XmlNativeLoader3 implements INativeLoader {

	private static final Logger LOG = Logger.getLogger(XmlNativeLoader3.class);
	
	public static final String IMPLEMENTATIONS = "implementations.xml";

	private Set<String> loadedLibIds = new HashSet<>();
	
	private boolean loaded;

	protected class ImplementationsHandler extends DefaultHandler
	{
		private String prefix="";
		public NativesToLoad nativesToLoad=new NativesToLoad();
		private Stack<Boolean> loadThis=new Stack<>();
		public ImplementationsHandler() {
			loadThis.push(true);
		}
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("filter".equals(localName))
			{
				boolean matches=false;
				if(loadThis.peek())
				{
					String property=attributes.getValue("property");
					String regex=attributes.getValue("regex");
					String value="";
					if(property!=null)
					{
						value=System.getProperty(property);
					}
					matches=value.matches(regex);
				}
				loadThis.push(loadThis.peek()&&matches);
			}
			if(loadThis.peek())
			{
				if("include".equals(localName))
				{
					String path=attributes.getValue("path");
					if(path!=null)
					{
						URL resource=getResource(path);
						if(resource!=null)
						{
							String oldPrefix=prefix;
							try
							{
								String pathPrefix=attributes.getValue("path-prefix");
								prefix=prefix+pathPrefix==null?"":pathPrefix;
								parseUsingHandler(resource, this);
							}catch(Exception e)
							{
								LOG.error("parse Included file",e);
							}finally
							{
								prefix=oldPrefix;
							}
						}
					}
				}
				else if("lib".equals(localName))
				{
					final String path=prefix+attributes.getValue("path");
					final String idCandidate = attributes.getValue("id");
					final String id = idCandidate == null ? path : idCandidate;
					final String installPath=attributes.getValue("installPath");
					if (!loadedLibIds.contains(id)) {
						nativesToLoad.getBinaries().add(new NativeBinary(id, path, installPath));
						loadedLibIds.add(id);
					}
				}
			}
		}
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if("filter".equals(localName))
			{
				loadThis.pop();
			}
		}
		
	}
	
	/**
	 * Subclasses may override so they can use different file name.
	 * @return
	 */
	public String getNativesDeclarationResourceName()
	{
		return IMPLEMENTATIONS;
	}

	protected URL getResource(String path) {
		URL resource=getClass().getResource(path);
		return resource;
	}
	@Override
	public NativesToLoad getNatives(String arch, String name)
			throws NativeLoadException {
		ImplementationsHandler handler = new ImplementationsHandler();
		parseUsingHandler(getClass().getResource(getNativesDeclarationResourceName()), handler);
		return handler.nativesToLoad;
	}

	private void parseUsingHandler(URL resource, DefaultHandler handler) {
		try (final InputStream istream = resource.openStream()) {
			UtilNativeLoader.createSAXParser().parse(istream, handler);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new NativeLoadException(e);
		}
	}
	
	/**
	 * Load the native library.
	 * On a singleton instance this method is re-callable: only the first call 
	 * will load the library.
	 */
	public void load() {
		synchronized (this) {
			if(!loaded)
			{
				loaded=true;
				UtilNativeLoader.loadNatives(this);
			}
		}
	}
}
