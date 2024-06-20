package hu.qgears.remote.main;

import java.io.File;

import hu.qgears.remote.AutoRestart;
import hu.qgears.remote.RemoteServer;
import joptsimple.tool.AbstractTools;

public class RemotingMain extends AbstractTools {
	public static void main(String[] args) {
		int retCode = new RemotingMain().mainEntryPoint(args);
		System.exit(retCode);
	}
	public static int autoRestartEntry(File f, String[] args) {
		AutoRestart.autoRestartArgs=new AutoRestart.Args();
		AutoRestart.autoRestartArgs.current=f;
		int retCode = new RemotingMain().mainEntryPoint(args);
		return retCode;
	}
	@Override
	protected void registerTools() {
		registerTools(this);
	}
	public static void registerTools(AbstractTools parent)
	{
		parent.register(new RemoteServer());
		parent.register(new AutoRestart());
		parent.register(new SyncFolderTo());
	}
}
