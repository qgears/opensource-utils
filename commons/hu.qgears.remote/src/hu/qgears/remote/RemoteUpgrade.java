package hu.qgears.remote;

import java.io.File;
import java.net.InetSocketAddress;

import hu.qgears.commons.UtilFile;
import hu.qgears.coolrmi.CoolRMIClient;
import joptsimple.tool.AbstractTool;

public class RemoteUpgrade extends AbstractTool {
	
	public static class RemoteUpgradeArgs extends ConnectionArgs
	{
		public File jar;
		
		@Override
		public void validate() {
			super.validate();
			if (jar==null || !jar.isFile()) {
				throw new IllegalArgumentException("jar must be an existing file");
			}
		}
	}	
	@Override
	public String getId() {
		return "remoteUpgrade";
	}

	@Override
	public String getDescription() {
		return "Upgrade remote build server";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		RemoteUpgradeArgs args=(RemoteUpgradeArgs)a;
		CoolRMIClient c=new CoolRMIClient(RemoteUpgrade.class.getClassLoader(), new InetSocketAddress(args.host, args.port), true);
		IRemoteIf s=(IRemoteIf)c.getService(IRemoteIf.class, IRemoteIf.id);
		System.out.println("Upload jar : "+args.jar + " to "+args.host + ":"+args.port);
		s.updateProgram(UtilFile.loadFile(args.jar));
		c.close();
		return 0;
	}

	@Override
	protected IArgs createArgsObject() {
		return new RemoteUpgradeArgs();
	}
}
