package hu.qgears.remote;

import java.util.Map;

public interface IRemoteIf {
	public static final String id="RemofeIf";
	/**
	 * Lock the "target" resource.
	 * Lock will be freed when client is disconnected.
	 * @return non null means successful locking. The object can be used to unlock or COOLRMI disconnect also unlcoks the lock.
	 */
	public ILockLocked lockTarget(String whoAmI);
	public String getCurrentLockOwner();
	public IFolderUpdateProcess updateFolder(IRemoteFile host, RemoteFolderData target);
	public IRemoteFile downloadFolder(String targetPath) throws Exception;
	public IProcessCallback executeCommand(String[] command, ICallback stdout, ICallback stderr) throws Exception;
	public void updateProgram(byte[] jar) throws Exception;
	public Map<String,String> getBuilderConfiguration();
}
