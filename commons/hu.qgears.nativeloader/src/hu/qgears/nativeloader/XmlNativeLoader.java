package hu.qgears.nativeloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * NativeLoader which fetches library data from an XML file.
 * 
 * The implementing class file should be accompanied with the binaries and an
 * XML file called natives-def.xml. The syntax is as follows:
 * 
 * <dl>
 * <dt><code>&lt;natives-def&gt;</code></dt>
 * <dd>
 * Top level element of the document.</dd>
 * 
 * <dt><code>&lt;platform&gt;</code></dt>
 * <dd>
 * Describes conditional loading of libraries. The libraries referenced inside
 * this element are loaded if the supplied arguments match the corresponding
 * system properties. May be embedded recursively.
 * 
 * Arguments:
 * <dl>
 * <dt><code>arch</code>, optional</dt>
 * <dd>Regular expression, {@link String#matches(String) matched} with 
 * the {@code os.arch} JVM system property.</dd>
 * <dt><code>name</code>, optional</dt>
 * <dd>Regular expression, {@link String#matches(String) matched} with
 * the {@code os.arch} JVM system property.</dd>
 * <dt><code>distroId</code>, optional in Linux, ignored in Windows</dt>
 * <dd>Regular expression, see {@link XmlHandler#AT_LINUX_DISTRO_ID}.</dd>
 * <dt><code>distroVer</code>, optional in Linux, ignored in Windows</dt>
 * <dd>Regular expression, see {@link XmlHandler#AT_LINUX_DISTRO_VERSION_ID}.</dd>
 * </dl>
 * </dd>
 * 
 * <dt><code>&lt;lib&gt;</code></dt>
 * <dd>
 * Describes a library to be loaded. Libraries are loaded in order they are
 * given in the file (they should be topologically shorted with regards to
 * dependencies).
 * 
 * Arguments:
 * <dl>
 * <dt><code>id</code>, optional</dt>
 * <dd>Identifier for the library to be loaded. This identifier has to be unique
 * to all entries of the same library to ensure that only the first matching one
 * will be loaded. 
 * <dt><code>path</code>, required</dt>
 * <dd>The path to the library to be loaded.
 * </dl>
 * </dd>
 * 
 * <dt><code>&lt;libs&gt;</code></dt>
 * <dd>
 * A logical group of libraries.
 * 
 * Arguments:
 * <dl>
 * <dt><code>name</code>, optional</dt>
 * <dd>Name of the group for reference.
 * <dt><code>enabled</code>, optional</dt>
 * <dd>Whether the group is loaded. May be <code>true</code> or
 * <code>false</code>; default: <code>true</code>.
 * </dl>
 * </dd>
 * </dl>
 * 
 * Example natives-def.xml file:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <natives-def>
 *     <platform arch="x86|i[3-6]86">
 *         <platform name="Linux">
 *             <lib path="libhello.so" />
 *         </platform>
 *         <platform name="Windows .+">
 *             <lib path="hello.dll" />
 *         </platform>
 *     </platform>
 *     <platform arch="(amd|x86_)64">
 *         <platform name="Linux">
 *             <lib path="libhello64.so" />
 *         </platform>
 *         <platform name="Windows .+">
 *             <lib path="hello64.dll" />
 *         </platform>
 *     </platform>
 * </natives-def>
 * }
 * </pre>
 * 
 * @author KRiS
 * 
 */
public abstract class XmlNativeLoader implements INativeLoader {

	public static final String NATIVES_DEF = "natives-def.xml";

	/**
	 * On Linux, the {@literal /etc/os-release} file will be examined for
	 * information on Linux distribution name and version.
	 */
	public static final String OS_RELEASE_PLATFORM = "Linux";
	/**
	 * The file, from which Linux distribution name and version will be 
	 * extracted. If this file is not found, a default of the native 
	 * libraries will be loaded.
	 */
	public static final File OS_RELEASE_FILE  = new File("/etc/os-release");
	
	/**
	 * Encoding of the {@link #OS_RELEASE_FILE}, which is, by default, 
	 * {@link StandardCharsets#UTF_8}, but can be overridden by the
	 * {@code hu.qgears.nativeloader.os_release_charset} system property.
	 */
	public static final Charset OS_RELEASE_ENCODING = Charset.forName(
			System.getProperty("hu.qgears.nativeloader.os_release_charset", 
					StandardCharsets.UTF_8.name()));
	
	/**
	 * Name of the distribution machine-parseable ID parameter in the 
	 * {@literal /etc/os-release} file 
	 */
	public static final String OS_RELEASE_PROPNAME_ID = "ID";
	/**
	 * Name of the version identifier parameter in the 
	 * {@literal /etc/os-release} file 
	 */
	public static final String OS_RELEASE_PROPNAME_VERSION_ID = "VERSION_ID";
	
	private Properties osReleaseProperties;
	
	
	
	/**
	 * Subclasses may override so they can use different file name.
	 * @return
	 */
	public String getNativesDeclarationResourceName()
	{
		return NATIVES_DEF;
	}

	/**
	 * Loads properties from the {@value #OS_RELEASE_FILE} in case if the value 
	 * {@code os} parameter is 'Linux'. This method also removes leading
	 * and trailing quotation marks from the 
	 * {@value #OS_RELEASE_PROPNAME_VERSION_ID} value for convenience.
	 * @param os the name of the operation system
	 */
	private void loadOsReleaseFile(final String os) {
		if (OS_RELEASE_PLATFORM.equals(os) && OS_RELEASE_FILE.exists()) {
			try (final InputStreamReader osReleaseReader = new InputStreamReader(
					new FileInputStream(OS_RELEASE_FILE), OS_RELEASE_ENCODING)) {
				osReleaseProperties = new Properties();
				osReleaseProperties.load(osReleaseReader);
				
				/* Postprocessing: removing quotation marks from VERSION_ID */
				final String osVersionIdRaw = osReleaseProperties.getProperty(
						OS_RELEASE_PROPNAME_VERSION_ID);
				if(osVersionIdRaw!=null)
				{
					osReleaseProperties.put(OS_RELEASE_PROPNAME_VERSION_ID, 
						osVersionIdRaw.replaceAll("[\"]", ""));
				}
			} catch (final IOException e) {
				throw new NativeLoadException("Exception while attempting "
						+ "to load Linux distribution version information "
						+ "from " + OS_RELEASE_FILE.getPath(), e);
			}
		}
	}

	private class OsAndArchAwareXmlHandler extends XmlHandler {

		protected final String arch;
		protected final String os;
		
		/**
		 * Initialize a new XmlHandler object.
		 * 
		 * @param arch
		 *            the arch
		 * @param os
		 *            the os name
		 */
		public OsAndArchAwareXmlHandler(String arch, String os) throws NativeLoadException {
			this.arch = arch;
			this.os = os;
		}
		
		@Override
		protected boolean isMatching(String archCond, String nameCond, 
				String linuxDistroIdCond, String linuxDistroVersionCond) {
			boolean match = true;
			if (archCond != null) {
				match &= arch.matches(archCond);
			}
			if (nameCond != null) {
				match &= os.matches(nameCond);
			}
			
			if (osReleaseProperties != null) {
				if (linuxDistroIdCond != null) {
					final String distroId = osReleaseProperties.getProperty(
							OS_RELEASE_PROPNAME_ID);
					match &= match(distroId, linuxDistroIdCond);
				}
				
				if (linuxDistroVersionCond != null) {
					final String versionId = osReleaseProperties.getProperty(
							OS_RELEASE_PROPNAME_VERSION_ID);
					if(versionId!=null)
					{
						match &= versionId.matches(linuxDistroVersionCond);
					}
				}
			}
			
			return match;
		}
		
		private boolean match(String value, String pattern) {
			if(value==null)
			{
				return false;
			}else
			{
				return value.matches(pattern);
			}
		}
	}
	
	@Override
	public NativesToLoad getNatives(String arch, String os)
			throws NativeLoadException {
		loadOsReleaseFile(os);
		XmlHandler handler = new OsAndArchAwareXmlHandler(arch, os);
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			InputStream istream = getClass().getResourceAsStream(getNativesDeclarationResourceName());
			InputSource isource = new InputSource(istream);
			reader.setContentHandler(handler);
			reader.parse(isource);
		} catch (SAXException e) {
			throw new NativeLoadException(e);
		} catch (IOException e) {
			throw new NativeLoadException(e);
		}
		return new NativesToLoad(handler.getPreloads(), handler.getNatives(), handler.sources);
	}
}
