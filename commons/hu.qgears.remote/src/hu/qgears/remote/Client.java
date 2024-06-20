package hu.qgears.remote;

import java.io.IOException;
import java.net.InetSocketAddress;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.coolrmi.CoolRMIClient;

public class Client implements NoExceptionAutoClosable {
	CoolRMIClient c;
	public Client(ConnectionArgs args) throws IOException {
		c=new CoolRMIClient(Client.class.getClassLoader(), new InetSocketAddress(args.host, args.port), true);
		c.setServiceRegistry(RemotingConfiguration.createConfiguration());
		c.setTimeoutMillis(RemoteServer.timeoutMillis);
	}
	public IRemoteIf getIf() throws IOException
	{
		IRemoteIf s=(IRemoteIf)c.getService(IRemoteIf.class, IRemoteIf.id);
		return s;
	}
	@Override
	public void close() {
		try {
			c.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
