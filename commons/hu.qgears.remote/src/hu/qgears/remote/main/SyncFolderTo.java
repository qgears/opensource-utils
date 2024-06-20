package hu.qgears.remote.main;

import java.io.File;
import java.net.InetSocketAddress;

import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.remote.Client;
import hu.qgears.remote.FolderUpdateProcess;
import hu.qgears.remote.IFolderUpdateProcess;
import hu.qgears.remote.IRemoteIf;
import hu.qgears.remote.RemoteFileHost;
import hu.qgears.remote.RemoteFolderData;
import hu.qgears.remote.RemotingConfiguration;
import joptsimple.tool.AbstractTool;

public class SyncFolderTo extends AbstractTool {

	@Override
	public String getId() {
		return "syncFolder";
	}

	@Override
	public String getDescription() {
		return "Sync a folder to a remoting server";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		SyncFolderToArgs args=(SyncFolderToArgs) a;
		
		{
			File toSync=args.src;
			if(toSync==null)
			{
				throw new RuntimeException();
			}
			long t0=System.currentTimeMillis();
			RemoteFolderData rfd=new RemoteFolderData();
			rfd.targetPath=args.tg;
			rfd.absolutePath=args.absolutePath;
			rfd.addFolder("", toSync);
			System.out.println("Folder summed "+(System.currentTimeMillis()-t0)+"millis");
			CoolRMIClient c=new CoolRMIClient(Client.class.getClassLoader(), new InetSocketAddress(args.host, args.port), true);
			c.setTimeoutMillis(args.timeoutMs);
			try
			{
				c.setServiceRegistry(RemotingConfiguration.createConfiguration());
				// Get service object by id and interface
				IRemoteIf s=(IRemoteIf)c.getService(IRemoteIf.class, IRemoteIf.id);
				IFolderUpdateProcess ifup=s.updateFolder(new RemoteFileHost(rfd), rfd);
				FolderUpdateProcess.waitFinish(ifup, args.timeoutMs);
				System.out.println("Folder sync finished "+(System.currentTimeMillis()-t0)+"millis");
				// IProcessCallback cb=s.executeCommand(new String[]{"cat"}, new CallbackImpl(System.out).setCloseAllowed(false), new CallbackImpl(System.err).setCloseAllowed(false));
				// cb.data("Hello Kitten!\n".getBytes(StandardCharsets.UTF_8));
				// cb.close();
				// Thread.sleep(1000);
				// cb.destroy();
				// System.out.println("Exit value: "+cb.exitValue(2000));
			}finally
			{
				c.close();
			}
		}
		return 0;
	}
	
	@Override
	protected IArgs createArgsObject() {
		return new SyncFolderToArgs();
	}
}
