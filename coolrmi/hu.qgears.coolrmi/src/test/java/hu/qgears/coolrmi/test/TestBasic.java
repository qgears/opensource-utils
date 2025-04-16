package hu.qgears.coolrmi.test;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;
import hu.qgears.coolrmi.streams.JavaPipeClientConnectionFactory;
import hu.qgears.coolrmi.streams.JavaPipeServer;
import hu.qgears.coolrmi.streams.JavaPipeServerFactory;
import hu.qgears.coolrmi.test.rmiservice.CallbackImpl;
import hu.qgears.coolrmi.test.rmiservice.IService;
import hu.qgears.coolrmi.test.rmiservice.RemotingConfiguration;
import hu.qgears.coolrmi.test.rmiservice.Service;

public class TestBasic {
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
		server.getServiceRegistry().addService(new CoolRMIService(IService.id, IService.class, new Service()));
		server.start();
		CoolRMIClient client=new CoolRMIClient(getClass().getClassLoader(), new JavaPipeClientConnectionFactory(jps), true);
		// Configure callback proxy type and serialization replace type
		client.setServiceRegistry(RemotingConfiguration.createConfiguration());
		IService srv=(IService)client.getService(IService.class, IService.id);
		Assert.assertEquals("alma\nalma\n", srv.echo("alma", 2));
		int[] arr=new int[]{42};
		IntBuffer ib=IntBuffer.wrap(arr);
		Assert.assertEquals("First value in buffer: 42", srv.nonSerializableArgument(ib));
		CallbackImpl cbi=new CallbackImpl(client, srv);
		long delayMillis=100;
		long t0=System.nanoTime();
		srv.initTimer(cbi, delayMillis);
		Assert.assertEquals("Millis elapsed: 100", cbi.returns.get());
		long t1=System.nanoTime();
		long elapsedMillis=(t1-t0)/1000/1000;
		System.out.println("Elapsed: "+elapsedMillis);
		Assert.assertTrue(delayMillis-10<elapsedMillis);
	}
}
