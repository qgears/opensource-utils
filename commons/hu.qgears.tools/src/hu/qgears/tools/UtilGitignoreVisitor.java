package hu.qgears.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;

import hu.qgears.commons.UtilString;

/**
 * Simple file system visitor that respects .gitignore files.
 * gitignore implementation is based in JGit. 
 */
public class UtilGitignoreVisitor {
	List<IgnoreNode> ignoreLevels=new ArrayList<>();
	final public void visit(File dir, IgnoreNode current) throws Exception
	{
		ignoreLevels.add(current);
		List<String> localpath=new ArrayList<>();
		visit(dir, localpath, 0);
	}
	private void visit(File dir, List<String> localPath, int depth) throws Exception
	{
		boolean visitChildren=visited(dir, localPath, depth);
		if(visitChildren&&dir.isDirectory())
		{
			try
			{
				ignoreLevels.add(parseIgnoreFile(dir));
				File[] fs=dir.listFiles();
				if(fs!=null)
				{
					for(File f:fs)
					{
						localPath.add(f.getName());
						try
						{
							if(isIgnored(f, localPath, depth+1))
							{
								ignored(f, localPath, depth+1);
							}else
							{
								visit(f, localPath, depth+1);
							}
						}finally
						{
							localPath.remove(localPath.size()-1);
						}
					}
				}
			}finally
			{
				ignoreLevels.remove(ignoreLevels.size()-1);
			}
		}
	}

	private IgnoreNode parseIgnoreFile(File dir) throws FileNotFoundException, IOException {
		IgnoreNode ret=new IgnoreNode();
		File gitignore=new File(dir, ".gitignore");
		if(gitignore.isFile()&&gitignore.exists())
		{
			ret.parse(new FileInputStream(gitignore));
		}
		return ret;
	}
	protected void ignored(File f, List<String> localPath, int i) {
	}
	private boolean isIgnored(File f, List<String> localPath, int depth) {
		for(int i=ignoreLevels.size()-1; i>=0;--i)
		{
			IgnoreNode in=ignoreLevels.get(i);
			int keepParts=Math.min(ignoreLevels.size()-i, localPath.size());
			String entryPath=UtilString.concat(localPath.subList(localPath.size()-keepParts, localPath.size()), "/");
			MatchResult res=in.isIgnored(entryPath, f.isDirectory());
			if(MatchResult.IGNORED.equals(res))
			{
				return true;
			}else if(MatchResult.NOT_IGNORED.equals(res))
			{
				return false;
			}
		}
		return false;
	}
	/**
	 * Subclasses must override this method to implement useful feature.
	 * @param dir folder or file currently visited.
	 * @param localPath the local path of the file relative to the root folder of visiting.
	 * @param depth depth below the entry folder. First folders are on depth level 0.
	 * @return true means that children must also be visited. False blocks visiting the children of current folder.
	 * @throws Exception
	 */
	protected boolean visited(File dir, List<String> localPath, int depth) throws Exception {
		return true;
	}
}
