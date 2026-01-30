package lwjgl.standalone;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

import java.io.File;

public class AudioAccessor extends XmlNativeLoader
{
	private static boolean inited=false;

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}
	
	@Override
	public String getNativesDeclarationResourceName() {
		return "natives-openal-def.xml";
	}
	
	public static synchronized void initLwjglNatives() throws NativeLoadException
	{
		if(!inited)
		{
			UtilNativeLoader.loadNatives(new AudioAccessor());
			inited=true;
		}
	}
}
