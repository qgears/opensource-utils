package hu.qgears.tools;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.ignore.IgnoreNode;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;

public class SrvAdmin extends AbstractTool 
{
	public class Args implements IArgs
	{
		public File src;
		public File tg;
		@Override
		public void validate() {
			if(src==null)
			{
				throw new IllegalArgumentException("src must not be null");
			}
			if(tg==null)
			{
				throw new IllegalArgumentException("tg must not be null. Will be cropped first!");
			}
		}
		
	}
	@Override
	public String getId() {
		return "srvadmin";
	}
	@Override
	public String getDescription() {
		return "Do some magic - do not use it!";
	}
	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}
	@Override
	protected int doExec(IArgs a) throws Exception {
		Args args=(Args) a;
		UtilFile.deleteRecursive(args.tg);
		IgnoreNode ignore=new IgnoreNode();
		StringWriter chmods=new StringWriter();
		UtilGitignoreVisitor ufv=new UtilGitignoreVisitor()
			{
				@Override
				protected boolean visited(File dir, List<String> localPath, int depth) throws Exception {
					if(dir.isFile())
					{
						String path=dir.getAbsolutePath();
						System.out.println("Found: "+path);
						File targetFile=new File(args.tg, UtilString.concat(localPath, "/"));
						targetFile.getParentFile().mkdirs();
						try {
							Files.copy(dir.toPath(), targetFile.toPath(), LinkOption.NOFOLLOW_LINKS);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(!Files.isSymbolicLink(dir.toPath()))
					{
						if(depth>0)
						{
							Set<PosixFilePermission> perm=Files.getPosixFilePermissions(dir.toPath());
							chmods.append("chmod "+UtilFile2.serializePerm(perm)+" '"+UtilString.concat(localPath, "/")+"'\n");
						}
						return true;
					}
					return true;
				}
			};
		ufv.visit(args.src, ignore);
		String permissions="permissions.sh";
		UtilFile.saveAsFile(new File(args.src, permissions), chmods.toString());
		UtilFile.deleteRecursive(new File(args.tg, permissions));
		Files.copy(new File(args.src, permissions).toPath(), new File(args.tg, permissions).toPath(), LinkOption.NOFOLLOW_LINKS);
		return 0;
	}
}
