package hu.qgears.remote.main;

import java.io.File;

import hu.qgears.remote.ConnectionArgs;

public class SyncFolderToArgs extends ConnectionArgs {
	public File src;
	public boolean absolutePath;
	public String tg;
	public int timeoutMs=1000000;
	@Override
	public void validate() {
		if(src==null || !src.isDirectory())
		{
			throw new IllegalArgumentException("src must be a folder");
		}
		if(tg==null)
		{
			throw new IllegalArgumentException("tg must not be null");
		}
	}
}
