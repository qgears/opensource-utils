package lwjgl.standalone;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

import java.awt.Frame;
import java.io.File;

import org.apache.log4j.Logger;

public class BaseAccessor extends XmlNativeLoader {
	
	private static final Logger LOG = Logger.getLogger(BaseAccessor.class);
	
	private static boolean inited = false;

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

	@Override
	public String getNativesDeclarationResourceName() {
		return "natives-base-def.xml";
	}

	public static synchronized void initLwjglNatives()
			throws NativeLoadException {

		if (!inited) {
			if ("Linux".equals(System.getProperty("os.name"))) {
				// HACK - initialize AWT so it loads its native libs that lwjgl
				// depend on: libmawt.so, libjawt.so
				// A must on Linux but wine hangs on this call for some unknown
				// reason :-)
				{
					try
					{
						// mawt is not on classpath because it has two versions:
						// headless and x11. The correct version is selected dynamically 
						// lwjgl requires the X11 enabled one
						File mawt = findMawt(new File(System.getProperty("java.home")));
						Runtime.getRuntime().load(mawt.getAbsolutePath());
					}catch(Throwable t)
					{
						if((t.getMessage()!=null)&&t.getMessage().indexOf("already loaded")<0)
						{
							// Do not log already loaded exception!
							LOG.error(t);
						}
						// Try fallback mawt loading techniqe: create and dispose a frame:
						new Frame().dispose();
					}
				}
				// manually load libjawt.so into vm, needed since Java 7
				try {
					System.loadLibrary("jawt");					
				}catch(Throwable e)
				{
					LOG.error("Java version does not support jawt manual load");
				}
			}
			UtilNativeLoader.loadNatives(new BaseAccessor());
			inited = true;
		}
	}
	/**
	 * Find the X11 enabled libmawt.so file in the JRE folder.
	 * @param dir JRE folder (and subfolders on recursive calls)
	 * @return
	 */
	private static File findMawt(File dir) {
		if (dir.isDirectory()) {
			if ("xawt".equals(dir.getName())) {
				return new File(dir, "libmawt.so");
			}
			File[] fs = dir.listFiles();
			if (fs != null) {
				for (File f : fs) {
					File mawt = findMawt(f);
					if (mawt != null) {
						return mawt;
					}
				}
			}
		}
		return null;
	}
}