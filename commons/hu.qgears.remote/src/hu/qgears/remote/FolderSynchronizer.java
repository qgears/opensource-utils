package hu.qgears.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.UtilFile;

/**
 * Synchronizes a given local directory with the remote pair. Provides easy API
 * to synch input and output folder.
 * 
 * @author agostoni
 *
 */
public class FolderSynchronizer {

	/**
	 * The working dir remote side.
	 */
	private String targetPath;
	private File localSyncDir;
	private RemoteFolderData upLoadData;
	private IRemoteIf service;
	private int timeOut = 60000;
	private List<String> outputPaths = new ArrayList<>();
	/**
	 * @param targetPath the working directory on remote side (relative path).
	 * @param localSyncDir the local directory to synch
	 * @param service an initialized instance of the {@link IRemoteIf}
	 */
	public FolderSynchronizer(String targetPath, File localSyncDir,IRemoteIf service) {
		super();
		this.targetPath = targetPath;
		this.localSyncDir = localSyncDir;
		this.service = service;
		
		upLoadData = new RemoteFolderData();
		upLoadData.targetPath = targetPath;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}
	
	
	/**
	 * Add a directory within {@link #localSyncDir} that has to be uploaded to the remote.
	 * 
	 * @param path Relative path to the directory to upload
	 * @throws Exception
	 */
	public void addInput(String path) throws Exception {
		upLoadData.addFolder(path, new File(localSyncDir,path));
	}
	/**
	 * Add directory that has to be downloaded from remote side. (for instance build output directories). 
	 * 
	 * @param path Relative path to directory to download
	 * @throws Exception
	 */
	public void addOutput(String path) throws Exception {
		outputPaths.add(path);
	//	addInput(path);
	}


	
	/**
	 * Uploads configured {@link #addInput(String) inputs} to remote side.
	 * 
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public void synchUp() throws TimeoutException, ExecutionException {
		RemoteFileHost host = new RemoteFileHost(upLoadData);
		IFolderUpdateProcess ifup=service.updateFolder(host, upLoadData);
		FolderUpdateProcess.waitFinish(ifup, timeOut);
		
	}
	
	/**
	 * Downloads configured {@link #addOutput(String) outputs} into local synch dir.
	 * 
	 * @throws Exception
	 */
	public void synchDown() throws Exception {
		for (String toDownLoad : outputPaths) {
			IRemoteFile o = service.downloadFolder(targetPath + "/" + toDownLoad);
			FolderUpdateProcess fup=new FolderUpdateProcess(new File(localSyncDir,toDownLoad), o, o.getRemoteFolderData());
			fup.start();
			fup.finished.get(timeOut,TimeUnit.MILLISECONDS);
		}
	}
	
	
	public String getTargetPath() {
		return targetPath;
	}

	public void clean() {
		for (String toDownLoad : outputPaths) {
			UtilFile.deleteRecursive(new File(localSyncDir,toDownLoad));
		}
	}
}
