package hu.qgears.coolrmi.test;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.ICoolRMIProxy;
import hu.qgears.coolrmi.messages.CoolRMIReply;
import hu.qgears.coolrmi.remoter.CallAggregatorClientSideCompress;
import hu.qgears.coolrmi.remoter.CoolRMIProxy;
import hu.qgears.coolrmi.streams.JavaPipeClientConnectionFactory;
import hu.qgears.coolrmi.streams.JavaPipeServer;
import hu.qgears.coolrmi.streams.JavaPipeServerFactory;
import hu.qgears.coolrmi.test.rmiservice.IService;
import hu.qgears.coolrmi.test.rmiservice.RemotingConfiguration;
import hu.qgears.coolrmi.test.rmiservice.Service;

public class TestCallAggregator {
	class MyAggregator extends CallAggregatorClientSideCompress
	{
		public List<Throwable> exceptions=new ArrayList<>();
		public MyAggregator(CoolRMIProxy owner) {
			super(owner);
		}
		@Override
		protected void handleException(CoolRMIReply rep, Throwable exception) {
			exceptions.add(exception);
		}
	}
	/**
	 * Test basic features of remoting using a Java Pipe based local server/client setup:
	 * 
	 *  * simple method call
	 *  * special serializing of not serializable parameters.
	 *  * callback/proxy
	 *  
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testBasicRemoting() throws IOException, InterruptedException, ExecutionException
	{
		JavaPipeServer jps=new JavaPipeServer();
		CoolRMIServer server=new CoolRMIServer(getClass().getClassLoader(), new JavaPipeServerFactory(jps), true);
		Service serverSideService=new Service();
		server.getServiceRegistry().addService(new CoolRMIService(IService.id, IService.class, serverSideService));
		server.start();
		CoolRMIClient client=new CoolRMIClient(getClass().getClassLoader(), new JavaPipeClientConnectionFactory(jps), true);
		// Configure callback proxy type and serialization replace type
		client.setServiceRegistry(RemotingConfiguration.createConfiguration());
		IService srv=(IService)client.getService(IService.class, IService.id);
		CoolRMIProxy proxy=((ICoolRMIProxy)srv).getProxyObject();
		MyAggregator aggregator=new MyAggregator(proxy);
		proxy.setCallAggregator(aggregator);
		
		// Does not throw exception because it is aggregated and not called just now.
		srv.exceptionExample();
		Thread.sleep(15);
		// Remote is not called yet because void calls are aggregated
		Assert.assertEquals(0, serverSideService.getnCall());
		
		Assert.assertEquals("alma\nalma\n", srv.echo("alma", 2));
		Assert.assertEquals(1, aggregator.exceptions.size());
		Assert.assertTrue(aggregator.exceptions.get(0) instanceof RemoteException);
	}
}
