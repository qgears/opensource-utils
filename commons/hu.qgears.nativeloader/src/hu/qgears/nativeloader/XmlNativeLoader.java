package hu.qgears.nativeloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
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
 * <dd>
 * Regular expression.</dd>
 * <dt><code>name</code>, optional</dt>
 * <dd>
 * Regular expression.</dd>
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
	 * SAX handler that collects the natives for a specific platform.
	 * 
	 * @author KRiS
	 * 
	 */
	protected static class XmlHandler extends DefaultHandler {

		public static final String NAMESPACE = "";
		public static final String EL_NATIVESDEF = "natives-def";
		public static final String EL_PLATFORM = "platform";
		public static final String AT_PLATFORM_ARCH = "arch";
		public static final String AT_PLATFORM_NAME = "name";
		public static final String EL_LIBRARY = "lib";
		public static final String EL_SOURCE_ZIP = "srcZip";
		public static final String AT_LIBRARY_PATH = "path";
		public static final String AT_SOURCE_EXEC = "exec";
		public static final String AT_SOURCE_OUT = "out";
		public static final String AT_INSTALLPATH = "installPath";
		public static final String EL_LIBGROUP = "libs";
		public static final String AT_LIBGROUP_NAME = "name";
		public static final String AT_LIBGROUP_ENABLED = "enabled";
		public static final String AT_LIBGROUP_ENABLED_TRUE = "true";
		public static final String AT_LIBGROUP_ENABLED_FALSE = "false";

		protected final String arch;
		protected final String os;
		protected final List<NativeBinary> natives = new LinkedList<NativeBinary>();
		protected final List<SourceFile> sources = new ArrayList<SourceFile>(); 
		protected final Stack<Boolean> listening = new Stack<Boolean>();

		/**
		 * Initialize a new XmlHandler object.
		 * 
		 * @param arch
		 *            the arch
		 * @param os
		 *            the os name
		 */
		public XmlHandler(String arch, String os) {
			this.arch = arch;
			this.os = os;
		}

		@Override
		public void startDocument() throws SAXException {
			natives.clear();
			listening.clear();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (NAMESPACE.equals(uri)) {
				if (EL_NATIVESDEF.equals(localName)) { // <natives-def>
					listening.push(true);

				} else if (EL_PLATFORM.equals(localName)) { // <platform>
					if (listening.peek()) {
						String archCond = attributes.getValue(NAMESPACE,
								AT_PLATFORM_ARCH);
						String nameCond = attributes.getValue(NAMESPACE,
								AT_PLATFORM_NAME);
						boolean match = isMatching(archCond, nameCond);
						listening.push(match);

					} else {
						listening.push(false);
					}

				} else if (EL_LIBRARY.equals(localName)) { // <path>
					if (listening.peek()) {
						String libPath = attributes.getValue(NAMESPACE,
								AT_LIBRARY_PATH);
						if (libPath == null) {
							throw new SAXException("argument 'path' not "
									+ "supplied for element <library>");
						}
						String installPath = attributes.getValue(NAMESPACE,
								AT_INSTALLPATH);
						natives.add(new NativeBinary(libPath, installPath));
					}

				} else if (EL_SOURCE_ZIP.equals(localName)) { // <path>
					if (listening.peek()) {
						String srcPath = attributes.getValue(NAMESPACE,
								AT_LIBRARY_PATH);
						String srcExec = attributes.getValue(NAMESPACE,
								AT_SOURCE_EXEC);
						String srcOut = attributes.getValue(NAMESPACE,
								AT_SOURCE_OUT);
						String installPath = attributes.getValue(NAMESPACE,
								AT_INSTALLPATH);
						if (srcPath == null) {
							throw new SAXException("argument 'path' not "
									+ "supplied for element <"+EL_SOURCE_ZIP+">");
						}
						sources.add(new SourceFile(srcPath, srcExec, srcOut, installPath));
					}

				} 
				else if (EL_LIBGROUP.equals(localName)) { // <libs>
					if (listening.peek()) {
						String enabledStr = attributes.getValue(NAMESPACE,
								AT_LIBGROUP_ENABLED);
						if (enabledStr == null
								|| AT_LIBGROUP_ENABLED_TRUE.equals(enabledStr)) {
							listening.push(true);
						} else if (AT_LIBGROUP_ENABLED_FALSE.equals(enabledStr)) {
							listening.push(false);
						} else {
							throw new SAXException("Unknown argument value: "
									+ enabledStr);
						}

					} else {
						listening.push(false);
					}
				} else {
					throw new SAXException("Uknown element: " + qName);
				}
			}
		}

		protected boolean isMatching(String archCond, String nameCond) {
			boolean match = true;
			if (archCond != null) {
				match &= arch.matches(archCond);
			}
			if (nameCond != null) {
				match &= os.matches(nameCond);
			}
			return match;
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (NAMESPACE.equals(uri)) {
				if (EL_NATIVESDEF.equals(localName) // </natives-def>
						|| EL_PLATFORM.equals(localName) // </platform>
						|| EL_LIBGROUP.equals(localName)) { // </libs>
					listening.pop();

				} else if (EL_LIBRARY.equals(localName)||
						EL_SOURCE_ZIP.equals(localName)) { // </path>
					// is something needed here?

				} else {
					// we should never ever be here
					throw new AssertionError("unknown endElement: " + qName);
				}
			}
		}

		/**
		 * @return the arch
		 */
		public String getArch() {
			return arch;
		}

		/**
		 * @return the os
		 */
		public String getOs() {
			return os;
		}

		/**
		 * @return the natives
		 */
		public List<NativeBinary> getNatives() {
			return natives;
		}

	}
	
	/**
	 * Subclasses may override so they can use different file name.
	 * @return
	 */
	public String getNativesDeclarationResourceName()
	{
		return NATIVES_DEF;
	}

	@Override
	public NativesToLoad getNatives(String arch, String os)
			throws NativeLoadException {
		XmlHandler handler = new XmlHandler(arch, os);
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
		return new NativesToLoad(handler.getNatives(), handler.sources);
	}
}
