package hu.qgears.nativeloader;

import hu.qgears.commons.UtilFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


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
//	static Logger log = Logger.getLogger(UtilNativeLoader.class.getName());
	
	/**
	 * Tells whether the running JVM is 32 or 64 bit, allowing to properly
	 * choose which versions of native libraries to load. 
	 * Note that this might break in the future and is NOT usable in sandboxed
	 * environments, as some state. See the following pages for more information:
	 * <ul>
	 * <li>{@linkplain http://www.oracle.com/technetwork/java/hotspotfaq-138619.html#64bit_detection}</li>
	 * <li>{@linkplain http://stackoverflow.com/a/12105297}</li>
	 * </ul>
	 * <br/>
	 * This variable overrides the value queried by the 'os.arch' system 
	 * property when determining which native lib to load:
	 * <ul>
	 * <li>32 -> i386</li>
	 * <li>64 -> amd64</li>
	 * <li>null (unknown) -> value of 'os.arch' system property
	 * </ul>
	 */
	public static final String dataModelSysProp = System.getProperty("sun.arch.data.model");

	public static boolean VERBOSE = true;
	
	static {
		if (dataModelSysProp == null) {
			System.out.println("JVM architecture cannot be determined by " +
					"querying 'sun.arch.data.model' system property; falling " +
					"back to the value of 'os.arch'");
		}
	}
	
	private static void info(String message)
	{
		if (VERBOSE) {
			System.out.println("UtilNativeLoader: " + message);
		}
	}
	/**
	 * Load natives. See head comment of the class.
	 * 
	 * @param clazz
	 * @throws Throwable
	 */
	public static void loadNatives(INativeLoader nativeLoader)
			throws NativeLoadException {
		try {
			Class<?> clazz = nativeLoader.getClass();

			String archKey = "os.arch";
			String oskey = "os.name";
			String arch = dataModelSysProp == null ? System.getProperty(archKey)
					: dataModelSysProp.replace("32", "i386").replace("64", "amd64");
			String os = System.getProperty(oskey);
			info("Searching for natives for class: "
					+ clazz.getName() + " arch: " + arch + " os: " + os);
			NativesToLoad natives = nativeLoader.getNatives(arch, os);
			for (NativeBinary libPath : natives.getBinaries()) {
				loadNativeBinary(libPath, clazz, nativeLoader);
			}
		} catch (NativeLoadException e) {
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
			byte[] bs = loadFile(url);
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
			directory.delete();
			directory.mkdirs();
//			directory=new File("nativesTmp");
//			directory.mkdirs();
		}
		return directory;
	}

	/**
	 * Load the content from the URL into a byte array.
	 * 
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static byte[] loadFile(URL resource) throws IOException {
		InputStream is = resource.openStream();
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int n;
			while ((n = is.read(buffer)) > 0) {
				bos.write(buffer, 0, n);
			}
			return bos.toByteArray();
		} finally {
			is.close();
		}
	}
}
