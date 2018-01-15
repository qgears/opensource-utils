package hu.qgears.tools;

import hu.qgears.tools.rtemplate.RTemplateStandalone;
import joptsimple.tool.AbstractTools;

public class Tools extends AbstractTools {

	private static Tools instance = new Tools();

	public static void main(String[] args) {
		int ret = instance.mainEntryPoint(args);
		System.exit(ret);
	}

	public static Tools getInstance() {
		return instance;
	}

	@Override
	protected void registerTools() {
		register(new SrvAdmin());
		register(new GitToZip());
		register(new GitBackupUpdate());
		register(new SvnDiff());
		register(new RTemplateStandalone());
		register(new LogPortForward());
	}
}
