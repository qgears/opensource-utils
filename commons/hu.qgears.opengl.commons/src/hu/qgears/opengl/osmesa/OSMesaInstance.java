package hu.qgears.opengl.osmesa;

import java.io.File;
import java.util.Arrays;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.nativeloader.UtilNativeLoader;

public class OSMesaInstance {
	private static OSMesaInstance instance=new OSMesaInstance();

	public static OSMesaInstance getInstance() {
		return instance;
	}
	private OSMesaInstance()
	{
		UtilNativeLoader.loadNatives(new OsMesaAccessor());
		String s=System.getenv("LD_LIBRARY_PATH");
		boolean b=false;
		if(s!=null)
		{
			for(String p:UtilString.split(s, ","))
			{
				try
				{
					for(File f: UtilFile.listFiles(new File(p)))
					{
						if("libGL.so.1".equals(f.getName()))
						{
							if(Arrays.equals(UtilFile.loadFile(f), UtilFile.loadFile(getClass().getResource("libGL.so.1"))))
							{
								System.out.println("Correct libGL.so.1 preload is set up");
								b=true;
							}
						}
					}
				}catch(Exception e)
				{
					// NOSONAR Silent ignore
				}
			}
		}
		if(!b)
		{
			System.err.println("WARNING Correct libGL.so.1 preload is not set up!!! LD_LIBRARY_PATH='"+s+"'");
		}
		OSMesaNative.execPreload();
	}
}
