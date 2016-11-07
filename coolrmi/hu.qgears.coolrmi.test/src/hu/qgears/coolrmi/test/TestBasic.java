package hu.qgears.coolrmi.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.streams.JavaPipeClientConnectionFactory;
import hu.qgears.coolrmi.streams.JavaPipeServer;
import hu.qgears.coolrmi.streams.JavaPipeServerFactory;

public class TestBasic {
	@Test
	public void testBasicRemoting() throws IOException
	{
		JavaPipeServer jps=new JavaPipeServer();
		CoolRMIServer server=new CoolRMIServer(getClass().getClassLoader(), new JavaPipeServerFactory(jps), true);
		server.getServiceRegistry().addService(new CoolRMIService("test01", ITestService.class, new TestService()));
		server.start();
		CoolRMIClient client=new CoolRMIClient(getClass().getClassLoader(), new JavaPipeClientConnectionFactory(jps), true);
		ITestService srv=(ITestService)client.getService(ITestService.class, "test01");
		Assert.assertEquals("test alma", srv.call1("alma"));
	}
}
