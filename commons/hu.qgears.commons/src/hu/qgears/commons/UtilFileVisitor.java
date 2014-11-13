package hu.qgears.commons;

import java.io.File;

public class UtilFileVisitor {
	final public void visit(File dir) throws Exception
	{
		visit(dir, "");
	}
	private void visit(File dir, String localPath) throws Exception
	{
		boolean visitChildren=visited(dir, localPath);
		if(visitChildren&&dir.isDirectory())
		{
			File[] fs=dir.listFiles();
			if(fs!=null)
			{
				for(File f:fs)
				{
					String lp=localPath+f.getName();
					if(f.isDirectory())
					{
						lp=lp+"/";
					}
					visit(f, lp);
				}
			}
		}
	}

	protected boolean visited(File dir, String localPath) throws Exception {
		return true;
	}
}
