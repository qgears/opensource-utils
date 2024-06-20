package hu.qgears.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilMd5;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.coolrmi.messages.CoolRMICall;

public class FolderUpdateProcess extends Thread implements IFolderUpdateProcess {
	File targetFolder;
	IRemoteFile host; RemoteFolderData target;
	public FolderUpdateProcess(File targetFolder, IRemoteFile host, RemoteFolderData target) {
		this.targetFolder=targetFolder;
		this.host=host;
		this.target=target;
		setDaemon(true);
	}
	public SignalFutureWrapper<Boolean> finished=new SignalFutureWrapper<>();
	@Override
	public void run() {
		try {
			File tg=targetFolder;
			tg.mkdirs();
			Set<String> touchedFolders=new HashSet<>();
			Set<String> touchedFiles=new HashSet<>();
			// root folder is considered untouched
			touchedFolders.add("");
			long t0=System.currentTimeMillis();
			for(RemoteFileData fd: target.files)
			{
				File local=new File(tg, fd.localPath);
				boolean update=false;
				addTouchedFolders(touchedFolders, touchedFiles, fd.localPath);
				if(local.exists() && local.isFile())
				{
					update=!UtilMd5.getMd5(local).equals(fd.md5);
				}else
				{
					update=true;
				}
				if(update)
				{
					if(local.exists() && !local.isFile())
					{
						System.out.println("Delete folder: "+local.getAbsolutePath());
						UtilFile.deleteRecursive(local);
					}
					System.out.println("Create: "+local.getAbsolutePath());
					FilePart data=null;
					try(FileOutputStream fos=new FileOutputStream(local))
					{
						while(data==null || data.hasMore)
						{
							data=host.download(fd, data==null?0l:(data.at+data.data.length), IRemoteFile.MAX_FILE_PART_BYTES);
							local.getParentFile().mkdirs();
							fos.write(data.data);
						}
					}
				}else
				{
					CoolRMICall call=CoolRMICall.getCurrentCall();
					call.asyncCall(null);
					host.noChange(fd);
					// System.out.println("File is untouched: "+fd.localPath);
				}
			}
			System.out.println("Sync download finished: "+(System.currentTimeMillis()-t0)+"millis");
				new UtilFileVisitor() {
					protected boolean visited(File dir, String localPath) throws Exception {
						if(target.ignoreDelete.contains(localPath))
						{
							System.out.println("Ignore delete: "+localPath);
							return false;
						}
						if(dir.isDirectory())
						{
							if(touchedFolders.contains(localPath))
							{
								
							}else
							{
								System.out.println("To delete folder: "+localPath);
								UtilFile.deleteRecursive(dir);
							}
						}else if(dir.isFile())
						{
							if(touchedFiles.contains(localPath))
							{
								
							}else
							{
								System.out.println("To delete file: "+localPath);
								if(dir.getName().equals("build"))
								{
									System.out.println("TODO Ignore delete: "+localPath);
								}else
								{
									UtilFile.deleteRecursive(dir);
								}
							}
						}else
						{
							System.out.println("WUT: "+localPath);
							UtilFile.deleteRecursive(dir);
						}
						return true;
					};
				}.visit(tg);
		} catch (Exception e) {
			finished.ready(null, e);
		}
		finished.ready(true, null);
	}
	private void addTouchedFolders(Set<String> touchedFolders, Set<String> touchedFiles, String localPath) {
		touchedFiles.add(localPath);
		addTouchedFolders(touchedFolders, localPath);
	}
	private void addTouchedFolders(Set<String> touchedFolders, String localPath) {
		int lastindex=localPath.lastIndexOf('/');
		if(lastindex>0)
		{
			String newp=localPath.substring(0, lastindex);
			touchedFolders.add(newp+"/");
			addTouchedFolders(touchedFolders, newp);
		}
	}
	@Override
	public boolean isFinished() throws ExecutionException {
		try {
			return finished.get(1000, TimeUnit.MILLISECONDS);
		} catch(ExecutionException e)
		{
			throw e;
		} catch (Exception e) {
			return false;
		}
	}
	public static void waitFinish(IFolderUpdateProcess fup, int timeoutMillis) throws TimeoutException, ExecutionException
	{
		long t0Nano=System.nanoTime();
		long timeoutAtNano=t0Nano+1000l*1000l*timeoutMillis;
		while(true)
		{
			if(timeoutAtNano<System.nanoTime())
			{
				throw new TimeoutException("Wait for remote folder update exit timeout. Millis: "+timeoutMillis +"\n"+t0Nano+"\n"+timeoutAtNano+"\n"+System.nanoTime());
			}
			if(fup.isFinished())
			{
				return;
			}
		}

	}
}
