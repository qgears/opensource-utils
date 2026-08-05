package hu.qgears.remote;

import joptsimple.tool.AbstractTool.IArgs;

public class ConnectionArgs implements IArgs {
	public String host="localhost";
	public int port=9999;
	@Override
	public void validate() {
		
	}
	public ConnectionArgs setHost(String host) {
		this.host=host;
		return this;
	}

}
