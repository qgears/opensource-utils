package hu.qgears.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.signal.SignalFutureWrapper;

public class RemoteIf implements IRemoteIf {
	public final SignalFutureWrapper<Boolean> closed=new SignalFutureWrapper<>();
	RemoteServerArgs args;
	private String lockOwner;
	private List<LockLocked> lockObjects = new ArrayList<LockLocked>();
	public RemoteIf(RemoteServerArgs args) {
		this.args=args;
	}
	@Override
	public IFolderUpdateProcess updateFolder(IRemoteFile host, RemoteFolderData target) {
		File parent=args.workDir;
		System.out.println("parent: "+parent);
		String targetPath=target.targetPath;
		System.out.println("target: "+targetPath);
		File tg;
		if(target.absolutePath)
		{
			tg=new File(targetPath);
		}else
		{
			tg=new File(parent, targetPath);
		}
		FolderUpdateProcess ret=new FolderUpdateProcess(tg, host, target);
		ret.start();
		return ret;
	}
	@Override
	public IProcessCallback executeCommand(String[] command, ICallback stdout, ICallback stderr) throws Exception {
		Process p=new ProcessBuilder(command).directory(args.workDir).start();
		new StreamSender(p.getInputStream(), stdout).start();
		new StreamSender(p.getErrorStream(), stderr).start();
		return new ProcessCallbackImpl(p.getOutputStream(), p);
	}
	@Override
	public void updateProgram(byte[] jar) throws Exception
	{
		File f=AutoRestart.autoRestartArgs.newFile();
		UtilFile.saveAsFile(f, jar);
		System.out.println("Update to: "+f);
		closed.ready(true, null);
	}
	@Override
	public Map<String, String> getBuilderConfiguration() {
		Properties ret=new Properties();
		try {
			try(InputStream inStream=new FileInputStream(new File("builder.properties")))
			{
				ret.load(inStream);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("props: "+ret);
		Map<String,String> r=new TreeMap<>();
		for(Object key:ret.keySet())
		{
			r.put(""+key, ret.getProperty(""+key));
		}
		// TODO Auto-generated method stub
		return r;
	}
	@Override
	public IRemoteFile downloadFolder(String targetPath) throws Exception {
		RemoteFolderData rfd=new RemoteFolderData();
		File tg=new File(args.workDir, targetPath);
		rfd.addFolder("", tg);
		rfd.targetPath=targetPath;
		return new RemoteFileHost(rfd);
	}
	@Override
	public synchronized ILockLocked lockTarget(String whoAmI) {
		if (whoAmI == null) {
			return null;
		}
		if (lockOwner == null) {
			lockOwner = whoAmI;
		}
		if(!whoAmI.equals(lockOwner))
		{
			return null;
		}
		lockOwner=whoAmI;
		LockLocked l = new LockLocked(this);
		lockObjects.add(l);
		return l;
	}
	@Override
	public synchronized String getCurrentLockOwner() {
		return lockOwner;
	}
	public synchronized void unlocked(LockLocked lockLocked) {
		lockObjects.remove(lockLocked);
		if (lockObjects.isEmpty()) {
			lockOwner=null;
		}
	}
}
