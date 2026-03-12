package lwjgl.standalone;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Configuration;

import hu.qgears.nativeloader.NativeLoadException;

public class BaseAccessor {
	
	private static final Logger LOG = Logger.getLogger(BaseAccessor.class);
	
	private static boolean inited = false;

	/**
	 * See {@link Configuration#OPENGL_CONTEXT_API}
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
	
	/**
	 * When Xless implementation is used (KMS, OSMesa+VNC, etc) then this has
	 * to be set to true before initializing this class to avoid error message:
	 * "java.awt.HeadlessException: No X11 DISPLAY variable was set, but this
	 * program performed an operation which requires it."
	 */
	public static boolean noX11 = false;

	public static synchronized void initLwjglNatives() {
		initLwjglNatives(ELwjglOpenGlImpl.DEFAULT);
	}
	public static synchronized void initLwjglNatives(ELwjglOpenGlImpl context)
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
			//Do not load opengLg until explicitly requested
			Configuration.OPENGL_EXPLICIT_INIT.set(true);
			if (context.optionName != null) {
				Configuration.OPENGL_CONTEXT_API.set(context.optionName);
			} else {
				//let the default impl to be loaded as lwjgl wants
			}
			//the goal of this call is only to trigger static initializers
			GL.getFunctionProvider();
			inited = true;
		}
	}
}
