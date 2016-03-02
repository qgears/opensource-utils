package hu.qgears.commons;

import java.io.File;

/**
 * Simple file system visitor that visits a folder with all subfolders and files.
 * @author rizsi
 *
 */
public class UtilFileVisitor {
	public final void visit(File dir) throws Exception
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

	/**
	 * Subclasses must override this method to implement useful feature.
	 * @param dir folder or file currently visited.
	 * @param localPath the local path of the file relative to the root folder of visiting.
	 * @return true means that children must also be visited. False blocks visiting the children of current folder.
	 * @throws Exception
	 */
	protected boolean visited(File dir, String localPath) throws Exception {
		return true;
	}
}
