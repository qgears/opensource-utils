package lwjgl.standalone;

import java.io.File;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.XmlNativeLoader;

public class LwjglBundleClassLoaderAccessor extends XmlNativeLoader
{
	private static boolean inited=false;
	@Override
	public void load(File nativeLibFile) throws Throwable {
		if(nativeLibFile.getName().indexOf("openal")>=0)
		{
			//TODO dont know how to do & test openal in lwjgl3
//			AL.alLib=nativeLibFile;
		}else
		{
			Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
		}
	}
	public static synchronized void initLwjglNatives() throws NativeLoadException
	{
		if(!inited)
		{
			BaseAccessor.initLwjglNatives();
			inited=true;
		}
	}
}
