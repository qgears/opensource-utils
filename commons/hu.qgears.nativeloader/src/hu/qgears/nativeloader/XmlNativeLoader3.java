package hu.qgears.nativeloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
 */
public abstract class XmlNativeLoader3 implements INativeLoader {

	private static final Logger LOG = Logger.getLogger(XmlNativeLoader3.class);
	
	public static final String IMPLEMENTATIONS = "implementations.xml";

	private Set<String> loadedLibIds = new HashSet<>();
	private Properties props = new Properties();
	
	private boolean loaded;
	public XmlNativeLoader3()
	{
	}

	private void loadOsDistributionInfo() {
		String os=System.getProperty("os.name");
		if("Linux".equals(os))
		{
			if(XmlNativeLoader.OS_RELEASE_FILE.exists())
			{
				try (final InputStreamReader osReleaseReader = new InputStreamReader(
						new FileInputStream(XmlNativeLoader.OS_RELEASE_FILE), XmlNativeLoader.OS_RELEASE_ENCODING)) {
					Properties osReleaseProperties = new Properties();
					osReleaseProperties.load(osReleaseReader);
					
					/* Postprocessing: removing quotation marks from VERSION_ID */
					final String osVersionIdRaw = osReleaseProperties.getProperty(
							XmlNativeLoader.OS_RELEASE_PROPNAME_VERSION_ID);
					if(osVersionIdRaw!=null)
					{
						osReleaseProperties.put(XmlNativeLoader.OS_RELEASE_PROPNAME_VERSION_ID, 
							osVersionIdRaw.replaceAll("[\"]", ""));
					}
					for(Object key: osReleaseProperties.keySet())
					{
						Object v=osReleaseProperties.get(key);
						String newKey="osrelease."+key;
						props.put(newKey, v);
					}
				} catch (final IOException e) {
					throw new NativeLoadException("Exception while attempting "
							+ "to load Linux distribution version information "
							+ "from " + XmlNativeLoader.OS_RELEASE_FILE.getPath(), e);
				}
			}
		}
	}

	protected class ImplementationsHandler extends DefaultHandler
	{
		private String prefix="";
		private List<NativeBinary> nativeBinaries = new ArrayList<NativeBinary>();
		private List<NativePreload> nativePreloads = new ArrayList<NativePreload>();
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
					String path=getValueReplaced(attributes, "path");
					if(path!=null)
					{
						URL resource=getResource(path);
						if(resource!=null)
						{
							String oldPrefix=prefix;
							try
							{
								String pathPrefix=getValueReplaced(attributes, "path-prefix");
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
					final String path=prefix+getValueReplaced(attributes, "resource");
					final String idCandidate = getValueReplaced(attributes, "id");
					final String id = idCandidate == null ? path : idCandidate;
					final String installPath=getValueReplaced(attributes, "installPath");
					if (!loadedLibIds.contains(id)) {
						nativeBinaries.add(new NativeBinary(id, path, installPath));
						loadedLibIds.add(id);
					}
				} else if("preload".equals(localName))
				{
					//System.out.println("WARNING: preload encountered");
					final String resource=prefix+getValueReplaced(attributes, "resource");
					final String fileName=getValueReplaced(attributes, "fileName");
					final String idCandidate = getValueReplaced(attributes, "id");
					final String id = idCandidate == null ? fileName : idCandidate;
					if (!loadedLibIds.contains(id)) {
						nativePreloads.add(new NativePreload(fileName, resource));
						loadedLibIds.add(id);
					}
				}
			}
		}
		private String getValueReplaced(Attributes attributes, String attName) {
			String raw=attributes.getValue(attName);
			if(raw==null)
			{
				return null;
			}else
			{
				StringBuilder ret=new StringBuilder();
				for(int i=0;i<raw.length();++i)
				{
					char ch=raw.charAt(i);
					if(ch=='$')
					{
						StringBuilder varname=new StringBuilder();
						i++;
						ch=raw.charAt(i);
						if(ch!='{')
						{
							throw new IllegalArgumentException("Illegal variable in attribute value: "+raw);
						}
						varnameExtract:
						for(i++;i<raw.length();++i)
						{
							ch=raw.charAt(i);
							if(ch=='}')
							{
								break varnameExtract;
							}else
							{
								varname.append(ch);
							}
						}
						String value=props.getProperty(varname.toString());
						if(value!=null)
						{
							ret.append(value);
						}else
						{
							LOG.error("Unknown argument: '"+varname.toString()+"'");
						}
					}else
					{
						ret.append(ch);
					}
				}
				return ret.toString();
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
		public NativesToLoad getNativesToLoad() {
			return new NativesToLoad(nativePreloads, nativeBinaries);
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
		return handler.getNativesToLoad();
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
				Properties sys=System.getProperties();
				props=new Properties();
				for(Object o: sys.keySet())
				{
					props.put(o, sys.get(o));
				}
				loadOsDistributionInfo();
				String arch=sys.getProperty("os.arch");
				switch (arch) {
				case "amd64":
					props.put("processed.arch", "amd64");
					break;
				case "x86_64":
					props.put("processed.arch", "amd64");
					break;
				case "i386":
					props.put("processed.arch", "i386");
					break;
				default:
					break;
				}
				UtilNativeLoader.loadNatives(this);
			}
		}
	}
}
