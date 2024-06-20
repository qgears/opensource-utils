package hu.qgears.remote;

import java.io.File;
import java.io.Serializable;

public class RemoteFileData implements Serializable {
	private static final long serialVersionUID = 1L;
	public File dir;
	public String localPath;
	public String md5;
	public int index;
	public RemoteFileData(File dir, String localPath, String md5, int index) {
		this.dir=dir;
		this.localPath=localPath;
		this.md5=md5;
		this.index=index;
	}
}
