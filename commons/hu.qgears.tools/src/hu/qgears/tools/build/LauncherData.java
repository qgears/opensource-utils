package hu.qgears.tools.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;

public class LauncherData {

	public String mainClass;
	public String jarName;
	public static List<LauncherData> parse(File launcher) {
		List<LauncherData> ret=new ArrayList<>();
		if(launcher.exists())
		{
			try {
				List<String> pieces=UtilString.split(UtilFile.loadAsString(launcher), " \r\n");
				if(pieces.size()!=2)
				{
					throw new RuntimeException("launcher.txt has to have exactly two tokens: jarName mainClass");
				}
				LauncherData ld=new LauncherData();
				ld.jarName=pieces.get(0);
				ld.mainClass=pieces.get(1);
				ret.add(ld);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return ret;
	}

}
