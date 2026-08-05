package hu.qgears.remote;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RemoteFileHost implements IRemoteFile {
	RemoteFolderData rfd;
	public RemoteFileHost(RemoteFolderData rfd) {
		this.rfd=rfd;
	}
	@Override
	public FilePart download(RemoteFileData fdRremote, long at, int maxSize) throws IOException {
		FilePart ret=new FilePart();
		RemoteFileData fd=rfd.files.get(fdRremote.index);
		File f=fd.dir;
		System.out.println("Download: "+f+" at: "+at+" masSize: "+maxSize);
		long l=f.length();
		long remaining=l-at;
		ret.data=new byte[(int)Math.min(remaining, maxSize)];
		ret.hasMore=remaining!=(long)ret.data.length;
		ret.at=at;
		loadPart(f, at, ret.data);
		return ret;
	}
	private void loadPart(File f, long at, byte[] data) throws FileNotFoundException, IOException {
		try(FileInputStream i = new FileInputStream(f))
		{
			i.getChannel().position(at);
			int remaining=data.length;
			int off=0;
			while(remaining>0)
			{
				int n=i.read(data, off, remaining);
				if(n<1)
				{
					throw new EOFException();
				}
				off+=n;
				remaining-=n;
			}
		}
	}
	@Override
	public void noChange(RemoteFileData fdRremote) {
		System.out.println("No change: "+fdRremote.localPath);
	}
	@Override
	public RemoteFolderData getRemoteFolderData() {
		return rfd;
	}
}
