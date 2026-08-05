package hu.qgears.remote;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import hu.qgears.coolrmi.CoolRMIClient;
import joptsimple.tool.AbstractTool;

public class CopyFrom extends AbstractTool {
	
	public static class CopyFromArgs extends ConnectionArgs
	{
		public String remoteLoc="UART2_Renesas_works";
		
		public int timeoutMillis=600000;
		
		@Override
		public void validate() {
			super.validate();
		}
	}	
	@Override
	public String getId() {
		return "copyFrom";
	}

	@Override
	public String getDescription() {
		return "Copy folder from remote location";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		CopyFromArgs args=(CopyFromArgs)a;
		CoolRMIClient c=new CoolRMIClient(CopyFrom.class.getClassLoader(), new InetSocketAddress(args.host, args.port), true);
		IRemoteIf s=(IRemoteIf)c.getService(IRemoteIf.class, IRemoteIf.id);
		System.out.println("Conf: "+ s.getBuilderConfiguration());
		IRemoteFile rf=s.downloadFolder(args.remoteLoc);
		FolderUpdateProcess fup=new FolderUpdateProcess(new File("/tmp/a"), rf, rf.getRemoteFolderData());
		fup.start();
		fup.finished.get(args.timeoutMillis,TimeUnit.MILLISECONDS);
		c.close();
		return 0;
	}

	@Override
	protected IArgs createArgsObject() {
		return new CopyFromArgs();
	}
}
