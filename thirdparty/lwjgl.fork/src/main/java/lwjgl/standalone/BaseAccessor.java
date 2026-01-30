package lwjgl.standalone;

import java.io.File;

import org.apache.log4j.Logger;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

public class BaseAccessor extends XmlNativeLoader {
	
	/**
	 * Part of compatibility layer between LWJGL2 and 3. This constants does not
	 * change anything in case of LWJGL 2, but used in LWJGL 3.
	 */
	public static enum ELwjglOpenGlImpl {
		DEFAULT(null),
		NATIVE("native"),
		OSMESA("OSMesa"),
		EGL("EGL")
		;
		private String optionName;

		private ELwjglOpenGlImpl(String optionName) {
			this.optionName = optionName;
		}
		
	}
	
	private static final Logger LOG = Logger.getLogger(BaseAccessor.class);
	
	private static boolean inited = false;
	
	/**
	 * When Xless implementation is used (KMS, OSMesa+VNC, etc) then this has
	 * to be set to true before initializing this class to avoid error message:
	 * "java.awt.HeadlessException: No X11 DISPLAY variable was set, but this
	 * program performed an operation which requires it."
	 */
	public static boolean noX11 = false;

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

	@Override
	public String getNativesDeclarationResourceName() {
		return "natives-base-def.xml";
	}

	public static synchronized void initLwjglNatives() {
		initLwjglNatives(ELwjglOpenGlImpl.DEFAULT);
	}
	
	/**
	 * @param glContext Irrelevant in case of LWJGL 2, only exist to keep API-s compatible. 
	 * @throws NativeLoadException
	 */
	public static synchronized void initLwjglNatives(ELwjglOpenGlImpl glContext)
			throws NativeLoadException {

		if (!inited) {
			if (!noX11)
			{
				if ("Linux".equals(System.getProperty("os.name"))) {
					// manually load libjawt.so into vm, needed since Java 7+
					try {
						System.loadLibrary("jawt");					
					}catch(Throwable e)
					{
						LOG.error("Java version does not support jawt manual load");
					}
				}
			}
			UtilNativeLoader.loadNatives(new BaseAccessor());
			inited = true;
		}
	}
}
