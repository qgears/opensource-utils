package hu.qgears.coolrmi.test;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.Ignore;
import org.junit.Test;

import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.streams.TCPServerFactory;
import hu.qgears.coolrmi.test.rmiservice.IService;
import hu.qgears.coolrmi.test.rmiservice.Service;

//Not a real unit test
@Ignore
public class TestRemoteDelay {
	@Test
	public void testPerf () throws Exception {
		InetSocketAddress sa = new InetSocketAddress("localhost", 9876);
		CoolRMIServer server = startTcpServer(sa);
		try (	CoolRMIClient client = new CoolRMIClient(Service.class.getClassLoader(), sa, true)) {
			client.setTimeoutMillis(1000000);
			IService rrs = (IService) client.getService(IService.class, Service.id);
			
			for (int i = 0; i < 1000; i++) {
				rrs.echo("hello", 3000);
			}
		} finally {
			server.close();
		}
		
	}

	private CoolRMIServer startTcpServer(InetSocketAddress sa) throws IOException {
		CoolRMIServer server = new CoolRMIServer(Service.class.getClassLoader(),
				new TCPServerFactory(sa), true);
		server.getServiceRegistry().addService(new CoolRMIService(
				Service.id,
				IService.class, new Service()));
		server.start();
		return server;
	}
}
