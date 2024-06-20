package hu.qgears.remote;

import java.io.File;

public class RemoteServerArgs extends ConnectionArgs {
	@Override
	public void validate() {
		super.validate();
		if(workDir==null || !workDir.isDirectory())
		{
			throw new IllegalArgumentException("--workDir must exist");
		}
	}
	public File workDir;
}
