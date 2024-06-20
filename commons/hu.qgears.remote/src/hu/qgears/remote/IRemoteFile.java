package hu.qgears.remote;

import java.io.IOException;

public interface IRemoteFile {
	public static final int MAX_FILE_PART_BYTES=1024*1024*8;
	FilePart download(RemoteFileData fd, long position, int length) throws IOException;
	void noChange(RemoteFileData fdRremote);
	RemoteFolderData getRemoteFolderData();
}
