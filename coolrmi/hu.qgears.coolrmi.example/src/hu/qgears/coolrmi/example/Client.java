package hu.qgears.coolrmi.example;

import java.net.InetSocketAddress;
import java.nio.IntBuffer;

import hu.qgears.coolrmi.CoolRMIClient;

public class Client {
	public static void main(String[] args) throws Exception {
		// Create client by configuring the TCP client socket.
		// The client is already started after this command.
		CoolRMIClient c=new CoolRMIClient(Client.class.getClassLoader(), new InetSocketAddress("localhost", Server.port), true);
		// Configure callback proxy type and serialization replace type
		c.setServiceRegistry(RemotingConfiguration.createConfiguration());
		// Get service object by id and interface
		IService s=(IService)c.getService(IService.class, IService.id);
		// Use the service object:
		System.out.println(s.echo("Hello CoolRMI!", 3));
		int[] arr=new int[]{42};
		IntBuffer ib=IntBuffer.wrap(arr);
		System.out.println(s.nonSerializableArgument(ib));
		try
		{
			s.exceptionExample();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		// Service and CoolRMIClient is going to be closed by the callback.
		// The client is executed on a non-daemon thread so it keeps the Java process alive.
		s.initTimer(new CallbackImpl(c, s), 5000);
	}
}
