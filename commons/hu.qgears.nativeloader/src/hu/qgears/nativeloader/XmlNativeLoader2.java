package hu.qgears.nativeloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * NativeLoader which fetches library data from an XML file.
 * 
 * @author rizsi
 * 
 */
public abstract class XmlNativeLoader2 implements INativeLoader {

	private static final Logger LOG = Logger.getLogger(XmlNativeLoader2.class);
	
	public static final String IMPLEMENTATIONS = "implementations.xml";

	private Set<String> loadedLibIds = new HashSet<>();

	protected class ImplementationsHandler extends DefaultHandler
	{
		private List<NativeBinary> nativesToLoad;
		private boolean loadThis=false;
		public ImplementationsHandler() {
		}
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("implementation".equals(localName))
			{
				String path=attributes.getValue("path");
				if(path!=null)
				{
					URL resource=getResource(path);
					if(resource!=null)
					{
						try
						{
							NativesToLoad ret=checkAndParse(resource, path);
							if(ret!=null)
							{
								nativesToLoad = new ArrayList<>();
							}
						}catch(Exception e)
						{
							LOG.error("checkAndParse",e);
						}
					}
				}else
				{
					if(matches(attributes)&&nativesToLoad==null)
					{
						nativesToLoad = new ArrayList<>();
						loadThis=true;
					}
				}
			}
			else if(loadThis)
			{
				tryLoad(uri, localName, qName, attributes, nativesToLoad, "");
			}
		}
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if("implementation".equals(localName))
			{
				loadThis=false;
			}
		}
		
		public NativesToLoad getNativesToLoad() {
			return new NativesToLoad(nativesToLoad);
		}
		
	}
	
	class ImplHandler extends DefaultHandler
	{
		public List<NativeBinary> result;
		private String prefix;
		public ImplHandler(String prefix) {
			super();
			this.prefix = prefix;
		}
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("platform".equals(localName))
			{
				if(matches(attributes))
				{
					result = new ArrayList<>();
				}
			}
			if(result!=null)
			{
				tryLoad(uri, localName, qName, attributes, result, prefix);
			}
		}
	}
	
	private boolean matches(Attributes attributes)
	{
		String implName=attributes.getValue("name");
		String implArch=attributes.getValue("arch");
		return name.equals(implName)&&implArch.equals(arch);
	}
	
	private void tryLoad(String uri, String localName, String qName,
			Attributes attributes, List<NativeBinary> result, String prefix) {
		if("lib".equals(localName))
		{
			final String path=attributes.getValue("path");
			final String idCandidate = attributes.getValue("id");
			final String id = idCandidate == null ? path : idCandidate;
			final String installPath=attributes.getValue("installPath");
			
			if (!loadedLibIds.contains(id)) {
				result.add(new NativeBinary(id, prefix+path, installPath));
				loadedLibIds.add(id);
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

	private NativesToLoad checkAndParse(URL resource, String path) {
		String prefix=getPrefix(path);
		ImplHandler ih=new ImplHandler(prefix);
		parseUsingHandler(resource, ih);
		return new NativesToLoad(ih.result);
	}

	private String getPrefix(String path) {
		int idx=path.lastIndexOf("/");
		if(idx<0)
		{
			return "";
		}else
		{
			return path.substring(0, idx+1);
		}
	}

	protected URL getResource(String path) {
		URL resource=getClass().getResource(path);
		return resource;
	}
	private String arch;
	private String name;
	@Override
	public NativesToLoad getNatives(String arch, String name)
			throws NativeLoadException {
		this.arch=arch;
		this.name=name;
		ImplementationsHandler handler = new ImplementationsHandler();
		parseUsingHandler(getClass().getResource(getNativesDeclarationResourceName()), handler);
		return handler.getNativesToLoad();
	}

	private void parseUsingHandler(URL resource, DefaultHandler handler) {
		try (final InputStream istream = resource.openStream()) {
			UtilNativeLoader.createSAXParser().parse(istream, handler);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new NativeLoadException(e);
		}
	}
}
