package hu.qgears.remote;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilMd5;
import hu.qgears.remote.ignore.IgnoreHandler;

public class RemoteFolderData implements Serializable {
	private static final long serialVersionUID = 1L;
	public String targetPath;
	public boolean absolutePath;
	public RemoteFolderData() {
	}

	public List<RemoteFileData> files=new ArrayList<>();
	public Set<String> ignoreDelete=new TreeSet<>();

	public void addFolder(String rootPath, File folder) throws Exception {
		IgnoreHandler ih=new IgnoreHandler();
		new UtilFileVisitor()
		{
			@Override
			protected boolean visited(File dir, String localPath, int depth, NoExceptionAutoClosable[] toClose) throws Exception {
				if(dir.isDirectory())
				{
					File ignoreFile=new File(dir, ".syncignore");
					toClose[0]=ih.processIgnore(ignoreFile, depth);
				}
				if(ih.isIgnored(depth, localPath, dir.getName(), dir.isDirectory()))
				{
					System.out.println("Ignored: "+localPath);
					return false;
				}
				if(dir.isFile())
				{
					String md5=UtilMd5.getMd5(dir);
					String p=rootPath+localPath;
					int index=files.size();
					files.add(new RemoteFileData(dir, p, md5, index));
					System.out.println("File: "+localPath);
				}
				return super.visited(dir, localPath);
			}
		}
		.visit(folder);
	}
}
