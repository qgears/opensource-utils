package hu.qgears.nativeloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;


/**
 * Helper to load native libraries on multiple platforms.
 * 
 * Required: 
 * <ul>
 * <li>class that accesses native libraries _and_ natives-def.properties
 * file as local resource of the class:</li>
 * <li>natives-def.properties contains entries: 'osgi.arch'.'osgi.os'.index=bundle of native lib<br/>eg:
 * linux.x86_64.0=librizsivideo.so</li>
 * </ul>
 * <br/>
 * Trick - JVM implementation only allows access of loaded native libs from
 * classloader that invokes the runtime.loadLibrary(). That's why the parameter
 * must implement INativeLoader
 * 
 * @see #dataModelSysProp
 * 
 * @author rizsi
 */
public class UtilNativeLoader {
	
	private static final Logger LOG = Logger.getLogger(UtilNativeLoader.class);
	
	private UtilNativeLoader() {
		// disable constructor of utility class
	}
	
	/**
	 * Tells whether the running JVM is 32 or 64 bit, allowing to properly
	 * choose which versions of native libraries to load. 
	 * Note that this might break in the future and is NOT usable in sandboxed
	 * environments, as some state. 
	 * <br/>
	 * This variable overrides the value queried by the 'os.arch' system 
	 * property when determining which native lib to load:
	 * <ul>
	 * <li>32 -&gt; i386</li>
	 * <li>64 -&gt; amd64</li>
	 * <li>null (unknown) -&gt; value of 'os.arch' system property
	 * </ul>
	 */
	public static final String dataModelSysProp = System.getProperty("sun.arch.data.model");

	/*
	 * Variable name is ok here
	 */
	public static boolean VERBOSE = true;//NOSONAR
	
	static {
		if (dataModelSysProp == null) {
			info("JVM architecture cannot be determined by " +
					"querying 'sun.arch.data.model' system property; falling " +
					"back to the value of 'os.arch'");
		}
	}
	
	private static void info(String message)
	{
		if (VERBOSE) {
			LOG.info(message);
		}
	}
	/**
	 * Load natives. See head comment of the class.
	 * 
	 * @throws Throwable
	 */
	public static void loadNatives(INativeLoader nativeLoader)
			throws NativeLoadException {
		try {
			Class<?> clazz = nativeLoader.getClass();

			String archKey = "os.arch";
			String oskey = "os.name";
			String arch = System.getProperty(archKey);
			//hacky workarund to get proper arch info for arm64 platforms
			if (!arch.equals("aarch64")) {
				//TODO - i do not get the point of using dataModelSysProp here.
				// This should be removed, and make sure backward compatibility for existing natives-dex.xmls
				arch = dataModelSysProp == null ? System.getProperty(archKey)
						: dataModelSysProp.replace("32", "i386").replace("64", "amd64");
			}
			String os = System.getProperty(oskey);
			info("Searching for natives for class: "
					+ clazz.getName() + " arch: " + arch + " os: " + os);
			NativesToLoad natives = nativeLoader.getNatives(arch, os);
			for(NativePreload preload : natives.getPreloads())
			{
				ensurePreload(nativeLoader, preload);
			}
			for (NativeBinary libPath : natives.getBinaries()) {
				loadNativeBinary(libPath, clazz, nativeLoader);
			}
		} catch (NativeLoadException e) {//NOSONAR
			//rethrowing is OK, avoiding repacking if not required
			throw e;
		} catch (Throwable t) {
			throw new NativeLoadException(t);
		}
	}
	private static void loadNativeBinary(NativeBinary nativeBinary, Class<?> clazz,
			INativeLoader nativeLoader) throws Throwable {
		File g = null;
		if(nativeBinary.getInstallPath()!=null)
		{
			File f=new File(nativeBinary.getInstallPath());
			if(f.exists())
			{
				g=f;
				info("- Load native: "+g.getAbsolutePath());
			}
		}
		if(g==null)
		{
			URL url = nativeBinary.getUrl(clazz);
			if(url==null)
			{
				throw new NativeLoadException("Native not found: " + nativeBinary.getLibPath());
			}
			info("- Load native: " + nativeBinary.getLibPath()+" from: "+url);
			byte[] bs = UtilFile.loadFile(url);
			g = new File(getDirectory(), nativeBinary.getFileName());
			UtilFile.checkSaveAsFile(g, bs);
			info("Native is copied to temporary directory: "
							+ g.getAbsolutePath());
		}
		nativeLoader.load(g);
	}
	private static File directory;
	/**
	 * Get the temporary directory to unpack dynamic library files.
	 * @throws IOException 
	 */
	public synchronized static File getDirectory() throws IOException
	{
		if(directory==null)
		{
			// Use current working directory/nativesTmp
			directory=File.createTempFile("nativesTmp", "");
			//empty file, the delete must return true!
			if (!directory.delete()) {
				throw new IOException("Cannot create temp directory");
			}
			//a new directory on this path must be created
			if (!directory.mkdirs()){
				throw new IOException("Cannot create temp directory");
			}
		}
		return directory;
	}

	/**
	 * Make sure that a specific .so is on a preload path:
	 * <ul>
	 * <li>Read the "LD_LIBRARY_PATH" variable
	 * <li>Scan all paths of the variable for the given .so name
	 * <li>Compare first match to the required content
	 * <li>If equals then ok
	 * <li>If not equals then try to overwrite the file
	 * <li>If does not exist then create the file with the given content into 
	 *  the first LD_LIBRARY_PATH folder.
	 * </ul>
	 * @return {@code true} if the library is on a preload path, {@code false}
	 * otherwise or if the LD_LIBRARY_PATH is not defined
	 * @throws IOException either if checking the presence of a native library
	 * fails because of IO-related problem, or if automatic installation on a
	 * preload path fails
	 */
	public static boolean ensurePreload(INativeLoader nativeLoader, 
			NativePreload preload) throws IOException
	{
		URL res=nativeLoader.getClass().getResource(preload.getResource());
		boolean onPreloadPath = false;
		
		if (res == null) {
			LOG.error("ensurePreload: Resource does not exist: '"
					+ preload.getResource() + "' '" + preload.getFileName()
					+ "' accessor: " + nativeLoader.getClass().getName());
		}
		
		final String ldLibraryPathEnvVar = System.getenv("LD_LIBRARY_PATH");
		final List<String> preloadPaths;
		
		if (ldLibraryPathEnvVar == null) {
			preloadPaths = null;
		} else {
			preloadPaths = UtilString.split(ldLibraryPathEnvVar, ":");
		}
		
		if(preloadPaths == null || preloadPaths.isEmpty()) {
			LOG.error("ensurePreload: LD_LIBRARY_PATH is not "
					+ "set up: can not create '" + preload.getFileName() + "' for preload.");
		} else {
			iteratePreloadPaths:
			for (final String p: preloadPaths) {
				try {
					File folder = new File(p);
					File g = new File(folder, preload.getFileName());
					if (g.exists()) {
						if (Arrays.equals(UtilFile.loadFile(g), UtilFile.loadFile(res))) {
							LOG.info("ensurePreload: correct '" + preload.getFileName() 
									+ "' is already set up.");
							onPreloadPath = true;
						} else {
							LOG.warn("ensurePreload: Not correct version "
									+ "(different content) of '" + preload.getFileName()
									+ "' is set up on LD_LIBRARY_PATH. Path of file: '"
									+ g.getAbsolutePath() + "'. Try to overwrite...");
							
							break iteratePreloadPaths;
						}
					} else {
						
					}
				} catch(Exception e) {
					// NOSONAR Silent ignore
				}
			}
		
			if (!onPreloadPath && preloadPaths != null && !preloadPaths.isEmpty()) {
				// If the library is not on a preload path, installing automatically
				final String folderPath = preloadPaths.get(0);
				
				try {
					File folder = new File(folderPath);
					folder.mkdirs();
					File g = new File(folder, preload.getFileName());
					UtilFile.saveAsFile(g, UtilFile.loadFile(res));
					LOG.info("ensurePreload: correct '" + preload.getFileName()
							+ "' is set up by program into: " + g.getAbsolutePath());
					
					onPreloadPath = true;
				} catch (final IOException ioe) {
					LOG.error("Could not automatically copy " + preload.getFileName()
							+ " to its target preload path: " + folderPath, ioe);
					
					throw ioe;
				}
			}
		}
		
		return onPreloadPath;
	}

	/**
	 * Creates a {@link SAXParser SAX parser} suitable for secure processing 
	 * of the native load XML files.
	 * @return a secure {@link SAXParser} instance
	 * @throws ParserConfigurationException
	 * 		passed up if the SAX parser cannot be created
	 * @throws SAXNotRecognizedException
	 * 		passed up if the SAX parser cannot be created
	 * @throws SAXNotSupportedException
	 * 		passed up if the SAX parser cannot be created
	 * @throws SAXException
	 * 		passed up if the SAX parser cannot be created
	 */
	public static SAXParser createSAXParser()
			throws ParserConfigurationException, SAXNotRecognizedException, 
				SAXNotSupportedException, SAXException {
		final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		
		// Security settings; source:
		// https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
		saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		saxParserFactory.setFeature(
				"http://xml.org/sax/features/external-general-entities", false);
		saxParserFactory.setFeature(
				"http://xml.org/sax/features/external-parameter-entities", false);
		saxParserFactory.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd", 
				false); // Just to make sure
		saxParserFactory.setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
		saxParserFactory.setXIncludeAware(false);
		
		// Etc. functional settings
		saxParserFactory.setNamespaceAware(true);
		
		return saxParserFactory.newSAXParser();
	}
	
}
