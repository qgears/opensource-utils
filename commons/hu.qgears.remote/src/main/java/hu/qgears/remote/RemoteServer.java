package hu.qgears.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import joptsimple.tool.AbstractTool;

public class RemoteServer extends AbstractTool implements AutoCloseable {
	public static long timeoutMillis=20000;
	@Override
	public String getId() {
		return "remoteServer";
	}
	@Override
	public String getDescription() {
		return "Remoting server";
	}
	@Override
	protected int doExec(IArgs a) throws Exception {
		RemoteServerArgs args=(RemoteServerArgs) a;
		run(args);
		return 0;
	}
	CoolRMIServer s;
	RemoteIf rif;
	public void run(RemoteServerArgs args) throws IOException, InterruptedException, ExecutionException {
		rif=new RemoteIf(args);
		s=new CoolRMIServer(RemoteServer.class.getClassLoader(), new InetSocketAddress(args.host, args.port), true);
		s.setTimeoutMillis(timeoutMillis);
		s.setServiceRegistry(RemotingConfiguration.createConfiguration());
		s.getServiceRegistry().addService(new CoolRMIService(IRemoteIf.id, IRemoteIf.class, rif));
		s.start();
		System.out.println("Remoting server started: "+args.host+" "+args.port);
		try
		{
			rif.closed.get();
		}finally
		{
			s.close();
		}
	}
	@Override
	protected IArgs createArgsObject() {
		return new RemoteServerArgs();
	}
	@Override
	public void close() throws Exception {
		rif.closed.ready(true, null);
	}
}
