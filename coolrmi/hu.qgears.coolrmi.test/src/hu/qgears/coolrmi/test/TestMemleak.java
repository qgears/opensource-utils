package hu.qgears.coolrmi.test;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.commons.UtilMemory;
import hu.qgears.commons.UtilMemory.MemoryStatus;
import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.streams.JavaPipeClientConnectionFactory;
import hu.qgears.coolrmi.streams.JavaPipeServer;
import hu.qgears.coolrmi.streams.JavaPipeServerFactory;
import hu.qgears.coolrmi.test.bigmemservice.BigMemService;
import hu.qgears.coolrmi.test.bigmemservice.IBigMemService;

public class TestMemleak {
	/**
	 * Test for finding memory leak in remoting.
	 * If this test runs without problem then there is no "very big" memory leak.
	 */
	@Test
	public void testBasicRemoting() throws Exception, InterruptedException, ExecutionException
	{
		JavaPipeServer jps=new JavaPipeServer();
		CoolRMIServer server=new CoolRMIServer(getClass().getClassLoader(), new JavaPipeServerFactory(jps), true);
		server.getServiceRegistry().addService(new CoolRMIService(IBigMemService.id, IBigMemService.class, new BigMemService()));
		server.start();
		CoolRMIClient client=new CoolRMIClient(getClass().getClassLoader(), new JavaPipeClientConnectionFactory(jps), true);
		IBigMemService srv=(IBigMemService)client.getService(IBigMemService.class, IBigMemService.id);
		callOnce(srv);
		MemoryStatus mem0=UtilMemory.getMemoryUsed();
		for(int i=0;i<100;++i)
		{
			callOnce(srv);
		}
		MemoryStatus mem=UtilMemory.getMemoryUsed();
		// A maximum of 10MB of memory increment is allowed. Some memory increment is possible due to the
		// dynamic nature of the JVM.
		Assert.assertTrue(mem.getAllocated()<mem0.getAllocated()+10000000);
	}

	private void callOnce(IBigMemService srv) {
		srv.sendReceive(new byte[10000000]);
	}
}
