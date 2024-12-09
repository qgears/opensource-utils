package hu.qgears.commons;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple file system visitor that visits a folder with all subfolders and files.
 */
public class UtilFileVisitor {
	
	
	private boolean visitInAlphabeticOrder;
	
	public final void visit(File dir) throws Exception
	{
		visit(dir, "", 0);
	}
	private NoExceptionAutoClosable[] toClose=new NoExceptionAutoClosable[1];
	private void visit(File dir, String localPath, int depth) throws Exception
	{
		boolean visitChildren=visited(dir, localPath);
		visitChildren&=visited(dir, localPath, depth);
		toClose[0]=null;
		visitChildren&=visited(dir, localPath, depth, toClose);
		NoExceptionAutoClosable c=null;
		if(toClose[0]!=null)
		{
			c=toClose[0];
			toClose[0]=null;
		}
		try
		{
			if(visitChildren&&dir.isDirectory())
			{
				File[] fs=dir.listFiles();
				if(fs!=null)
				{
					List<File> dirContent;
					if (visitInAlphabeticOrder) {
						dirContent = new ArrayList<>(Arrays.asList(fs));
						Collections.sort(dirContent);
					} else {
						dirContent = Arrays.asList(fs);
					}
					
					for(File f:dirContent)
					{
						String lp=localPath+f.getName();
						if(f.isDirectory())
						{
							lp=lp+"/";
						}
						visit(f, lp, depth+1);
					}
				}
			}
		}finally
		{
			if(c!=null)
			{
				c.close();
			}
		}
	}

	public UtilFileVisitor() {
		//keep backward compatibility of API
		this(false);
	}
	
	/**
	 * @param visitInAlphabeticOrder If true, then traverse files of directories in
	 *                               alphabetical order (sorting files with
	 *                               {@link File#compareTo(File)})
	 */
	public UtilFileVisitor(boolean visitInAlphabeticOrder ) {
		setVisitInAlphabeticOrder(visitInAlphabeticOrder);
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
	/**
	 * Subclasses must override this method to implement useful feature.
	 * @param dir folder or file currently visited.
	 * @param localPath the local path of the file relative to the root folder of visiting.
	 * @param depth depth below the entry folder. First folders are on depth level 0.
	 * @return true means that children must also be visited. False blocks visiting the children of current folder.
	 * @throws Exception
	 */
	protected boolean visited(File dir, String localPath, int depth) throws Exception {
		return true;
	}
	/**
	 * Subclasses must override this method to implement useful feature.
	 * @param dir folder or file currently visited.
	 * @param localPath the local path of the file relative to the root folder of visiting.
	 * @param depth depth below the entry folder. First folders are on depth level 0.
	 * @param toClose output parameter array with length of 1. When toClose[0] is set to a {@link NoExceptionAutoClosable} object then that is called once the visiting of this subtree was finished.
	 * @return true means that children must also be visited. False blocks visiting the children of current folder.
	 * @throws Exception
	 */
	protected boolean visited(File dir, String localPath, int depth, NoExceptionAutoClosable[] toClose) throws Exception {
		return true;
	}
	
	/**
	 * Traverse files of directories in alphabetical order (sorting files with {@link File#compareTo(File)}) 
	 * @param visitInAlphabeticOrder
	 */
	public void setVisitInAlphabeticOrder(boolean visitInAlphabeticOrder) {
		this.visitInAlphabeticOrder = visitInAlphabeticOrder;
	}
	
	public boolean isVisitInAlphabeticOrder() {
		return visitInAlphabeticOrder;
	}
}
