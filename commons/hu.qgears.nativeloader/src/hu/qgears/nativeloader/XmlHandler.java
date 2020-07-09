package hu.qgears.nativeloader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
	 * SAX handler that collects the natives for a specific platform.
	 * 
	 * @author KRiS
	 */
public class XmlHandler extends DefaultHandler {

		public static final String NAMESPACE = "";
		public static final String EL_NATIVESDEF = "natives-def";
		public static final String EL_PLATFORM = "platform";
		public static final String AT_PLATFORM_ARCH = "arch";
		public static final String AT_PLATFORM_NAME = "name";
		/** 
		 * Optional XML {@link #EL_PLATFORM platform} tag parameter for 
		 * identifying the Linux distribution. It will be matched with the 
		 * {@code ID} entry in the {@code /etc/os-release} file.
		 */
		public static final String AT_LINUX_DISTRO_ID = "distroId";
		/** 
		 * Optional XML {@link #EL_PLATFORM platform} tag parameter for identifying
		 * the version of a Linux distribution. It will be matched with the 
		 * {@code VERSION_ID} entry in the {@code /etc/os-release} file. Note 
		 * that the value of the entry contains starting and trailing quotation
		 * marks.
		 */
		public static final String AT_LINUX_DISTRO_VERSION_ID = "distroVersion";
		public static final String EL_LIBRARY = "lib";
		public static final String EL_SOURCE_ZIP = "srcZip";
		/**
		 * Identifier for a library to ensure that a single instance is 
		 * attempted to be loaded if more than one is matching.
		 */
		public static final String AT_LIBRARY_ID = "id";
		public static final String AT_LIBRARY_PATH = "path";
		public static final String AT_INSTALLPATH = "installPath";
		public static final String AT_SOURCE_EXEC = "exec";
		public static final String AT_SOURCE_OUT = "out";
		public static final String EL_LIBGROUP = "libs";
		public static final String AT_LIBGROUP_NAME = "name";
		public static final String AT_LIBGROUP_ENABLED = "enabled";
		public static final String AT_LIBGROUP_ENABLED_TRUE = "true";
		public static final String AT_LIBGROUP_ENABLED_FALSE = "false";
		
		
		/**
		 * List of native binaries to be loaded.
		 */
		protected final List<NativeBinary> natives = new LinkedList<NativeBinary>();
		/**
		 * Identifiers of already matching native libraries. This set is used
		 * to avoid duplicate library loading. 
		 */
		protected final Set<String> nativesEnumd = new HashSet<>(); 
		protected final List<SourceFile> sources = new ArrayList<SourceFile>(); 
		protected final Stack<Boolean> listening = new Stack<Boolean>();

		

		@Override
		public void startDocument() throws SAXException {
			natives.clear();
			nativesEnumd.clear();
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
						String linuxDistroIdCond = attributes.getValue(NAMESPACE,
								AT_LINUX_DISTRO_ID);
						String linuxDistroVersionCond = attributes.getValue(NAMESPACE,
								AT_LINUX_DISTRO_VERSION_ID);
						boolean match = isMatching(archCond, nameCond,
								linuxDistroIdCond, linuxDistroVersionCond);
						listening.push(match);

					} else {
						listening.push(false);
					}

				} else if (EL_LIBRARY.equals(localName)) { // <path>
					if (listening.peek()) {
						final String libPath = attributes.getValue(NAMESPACE,
								AT_LIBRARY_PATH);
						final String libIdCandidate = attributes.getValue(NAMESPACE, 
								AT_LIBRARY_ID);
						final String libId = libIdCandidate == null 
								? libPath : libIdCandidate;
						
						if (libPath == null) {
							throw new SAXException("argument 'path' not "
									+ "supplied for element <library>");
						}
						String installPath = attributes.getValue(NAMESPACE,
								AT_INSTALLPATH);
						
						if (!nativesEnumd.contains(libId)) {
							natives.add(new NativeBinary(libId, libPath, installPath));
							nativesEnumd.add(libId);
						}
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

		protected boolean isMatching(String archCond, String nameCond, String linuxDistroIdCond,
				String linuxDistroVersionCond) {
			nativesEnumd.clear();
			return true;
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
		 * @return the natives
		 */
		public List<NativeBinary> getNatives() {
			return natives;
		}

	}