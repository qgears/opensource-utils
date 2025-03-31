package lwjgl.standalone;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;

import org.lwjgl.openal.AL;

public class LwjglBundleClassLoaderAccessor extends XmlNativeLoader
{
	private static boolean inited=false;
	@Override
	public void load(File nativeLibFile) throws Throwable {
		if(nativeLibFile.getName().indexOf("openal")>=0)
		{
			AL.alLib=nativeLibFile;
		}else
		{
			Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
		}
	}
	public static synchronized void initLwjglNatives() throws NativeLoadException
	{
		BaseAccessor.initLwjglNatives();
		InputAccessor.initLwjglNatives();
		if(!inited)
		{
			UtilNativeLoader.loadNatives(new LwjglBundleClassLoaderAccessor());
			inited=true;
		}
	}
}
