package hu.qgears.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import joptsimple.annot.JOHelp;

public class GitToZip extends AbstractTool
{
	public class Args implements IArgs
	{
		@JOHelp("Repository folder")
		public File repo;
		@JOHelp("Tag or branch to pack into a zip file")
		public String ref;
		@JOHelp("Only add files with this prefix to the generated zip file. Prefix is removed from file name.")
		public String path;
		@JOHelp("Add prefix to the file names generated.")
		public String addPrefix;
		@JOHelp("Output zip file (will be overwritten if exists. Parent folder must exist)")
		public File zip;
		@Override
		public void validate() {
			if(repo==null||!repo.isDirectory()||!repo.exists())
			{
				throw new IllegalArgumentException("repo must be an existing folder.");
			}
			if(ref==null)
			{
				throw new IllegalArgumentException("ref must be an existing branch or tag.");
			}
			if(zip==null)
			{
				throw new IllegalArgumentException("zip output file must be specified.");
			}
		}
	}
	@Override
	public String getId() {
		return "git2zip";
	}

	@Override
	public String getDescription() {
		return "Create a zip file from a git repository state reference (branch or tag). The created zip file is binary reproducible (as long as Java ZipOutputStrem is not updated).";
	}
	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}
	@Override
	public int doExec(IArgs aa) throws Exception {
		Args a=(Args) aa;
		Git git = Git.open(a.repo);
		Repository repository=git.getRepository();
		ObjectId obj= repository.resolve(a.ref);
		try(ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(a.zip)))
		{
			try(RevWalk revWalk = new RevWalk(repository, 1))
			{
			    RevCommit commit = revWalk.parseCommit(obj);
			    RevTree tree= commit.getTree();
			    try(TreeWalk treeWalk = new TreeWalk(repository))
			    {
				    treeWalk.addTree(tree);
				    treeWalk.setRecursive(true);
				    while(treeWalk.next())
				    {
				    	String p=treeWalk.getPathString();
				    	if(a.path!=null)
				    	{
				    		if(!p.startsWith(a.path) || p.length()<=a.path.length())
				    		{
				    			continue;
				    		}else
				    		{
				    			p=p.substring(a.path.length());
				    		}
				    	}
				    	if(a.addPrefix!=null)
				    	{
				    		p=a.addPrefix+p;
				    	}
				    	zos.putNextEntry(ReproducibleZipFile.fix(new ZipEntry(p), 0));
				    	try
				    	{
				            ObjectId objectId = treeWalk.getObjectId(0);
				            if(repository.hasObject(objectId))
				            {
								ObjectLoader loader = repository.open(objectId);
								loader.copyTo(zos);
				            }else
				            {
//				            	System.err.println("Omitting: "+treeWalk.getPathString());
				            }
				    	}finally
				    	{
				    		zos.closeEntry();
				    	}
				    }
			    }
			}
		}
		return 0;
	}
}
