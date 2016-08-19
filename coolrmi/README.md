# CoolRMI Java library

CoolRMI is a very lightweight Java remoting library. Features:

 * Minimal dependencies (hu.qgears.commons and hu.qgears.coolrmi packages)
 * Minimal boilerplate code
 * Callback is supported through the API using so called proxy objects
 * Single TCP session is used for remoting and callback
 * OSGI and Eclipse plugin compatible 
 * Plain Java Interfaces are used to define remote APIs
 * Java only - Java serialization is used to pass arguments and return values
 * Object replace technonolgy can be used to accomodate interfaces that have non-serializable arguments or return values 
    * Many existing interfaces can be used through CoolRMI without changing the original interface
 * Exceptions are passed through remoting. The client and the server stack trace is merged into a single trace

## Where should it be used

CoolRMI should be used in prototyping projects or in internal server-client architectures.

## Known security issues

CoolRMI uses Java serialization so it is vulnerable to attacks that build on Java serialization security issues. For this reason CoolRMI connections to not dependable clients or servers is not supported.

## What is the difference between a local and a CoolRMI remote interface?

 * Remote interfaces (and callback objects) may throw CoolRMIException. This is a not managed exception and can be caused by remoting problems (eg. connection timeout).
 * Remote proxy objects (service interfaces and callback interfaces) must be disposed (by casting them to ICoolRMIProxy).
 * Passing objects through reference is not possible. All arguments and return values are passed in a serialized form.

## Examples

See the full working example project: hu.qgears.coolrmi.example

The Server and Client classes can be started as an application after importing hu.qgears.commons, hu.qgears.coolrmi and hu.qgears.coolrmi.example into Eclipse.

Output of the client:

```
Hello CoolRMI!
Hello CoolRMI!
Hello CoolRMI!

First value in buffer: 42
java.rmi.RemoteException: This is thrown by the server. Stack traces on the server and the client are merged.
	at hu.qgears.coolrmi.example.Service.exceptionExample(Service.java:45)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at hu.qgears.coolrmi.remoter.CoolRMIRemoter$2.run(CoolRMIRemoter.java:267)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
	at java.lang.Thread.getStackTrace(Thread.java:1552)
	at hu.qgears.coolrmi.remoter.CoolRMIProxy.invoke(CoolRMIProxy.java:88)
	at com.sun.proxy.$Proxy0.exceptionExample(Unknown Source)
	at hu.qgears.coolrmi.example.Client.main(Client.java:24)
Callback returned: Millis elapsed: 5000
We close the CoolRMI service and then the application exits.
```

Service interface is just a Java interface:

```
package hu.qgears.coolrmi.example;

import java.nio.IntBuffer;
import java.rmi.RemoteException;

/**
 * Remote service example.
 * @author rizsi
 *
 */
public interface IService {
	/**
	 * Identifier of the service used to locate it through the remoting API.
	 */
	String id = "ExampleServiceV0.0.1";
	/**
	 * Simple method call with serializable arguments and return value.
	 * @param s
	 * @param x
	 * @return
	 */
	public String echo(String s, int x);
	/**
	 * Example for a service with a callback.
	 * See {@link RemotingConfiguration}
	 * @param cb callback interface is not passed a value but as a callback proxy.
	 * @param timeoutMs
	 */
	public void initTimer(ICallback cb, long timeoutMs);
	/**
	 * Example for passing non serializable argument.
	 * See {@link RemotingConfiguration}
	 * @param ib
	 * @return
	 */
	public String nonSerializableArgument(IntBuffer ib);
	/**
	 * Server throws an exception which is thrown on to the client.
	 * The stack traces are merged so it is easy to spot debug both sides for the problem.
	 * @throws RemoteException
	 */
	public void exceptionExample() throws RemoteException;
}
```

Implement the service just as a local object that implements the interface. The only difference is that proxy objects has to be disposed after usage:

```
package hu.qgears.coolrmi.example;

import java.nio.IntBuffer;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import hu.qgears.commons.UtilTimer;
import hu.qgears.coolrmi.ICoolRMIProxy;

public class Service implements IService
{

	@Override
	public String echo(String s, int x) {
		StringBuilder ret=new StringBuilder();
		for(int i=0;i<x;++i)
		{
			ret.append(s);
			ret.append("\n");
		}
		return ret.toString();
	}
	@Override
	public void initTimer(ICallback cb, long timeoutMs) {
		UtilTimer.getInstance().executeTimeout(timeoutMs, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				cb.callback("Millis elapsed: "+timeoutMs);
				// Cool RMI proxy objects must be disposed when not used any more.
				// The implementation allows local usage without remoting as well.
				if(cb instanceof ICoolRMIProxy)
				{
					((ICoolRMIProxy)cb).disposeProxy();
				}
				return null;
			}
		});
	}
	@Override
	public String nonSerializableArgument(IntBuffer ib) {
		return "First value in buffer: "+ib.get();
	}
	@Override
	public void exceptionExample() throws RemoteException {
		throw new RemoteException("This is thrown by the server. Stack traces on the server and the client are merged.");
	}
}
```

Launching a service with a given interface is so simple:

```
package hu.qgears.coolrmi.example;

import java.net.InetSocketAddress;

import hu.qgears.coolrmi.CoolRMIServer;
import hu.qgears.coolrmi.CoolRMIService;

public class Server {
	public static int port=9000;
	public static void main(String[] args) throws Exception {
		// Create server object coonfigured to a TCP port
		CoolRMIServer s=new CoolRMIServer(Server.class.getClassLoader(), new InetSocketAddress("localhost", port), true);
		// (We don't need to configure RMI on the server side because the server does not send proxy objects or
		// non-serializable replaced objects)
		// Add service to the server
		s.getServiceRegistry().addService(new CoolRMIService(IService.id, IService.class, new Service()));
		// Start the server (TCP port is opened here). The server runs on a non-dameon thread so it keeps the process alive.
		s.start();
	}
}

```

Using the service by id and interface:

```
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
```

The configuration of a replace object is a bit more complex but it is only necessary in cases of non-serializable arguments on the interface API.

```
package hu.qgears.coolrmi.example;

import java.nio.IntBuffer;

import hu.qgears.coolrmi.remoter.CoolRMIReplaceEntry;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import hu.qgears.coolrmi.remoter.IReplaceSerializable;

public class RemotingConfiguration {
	/**
	 * Object replace entry for replacing non-serializable object through the CoolRMI remoting API
	 * with a serializable object.
	 * 
	 * Any superclass or interface of the actual object is enough to be defined as a replace entry.
	 * 
	 * (In our case this entry will replace all subclasses of IntBuffer: DirectIntBuffer, HeapIntBuffer, etc.)
	 * @author rizsi
	 *
	 */
	static class IntBufferReplace extends CoolRMIReplaceEntry
	{

		public IntBufferReplace() {
			super(IntBuffer.class);
		}

		@Override
		public IReplaceSerializable doReplace(Object o) {
			IntBuffer ib=(IntBuffer) o;
			int[] ints=new int[ib.remaining()];
			ib.get(ints);
			ib.position(ib.position()-ints.length);
			return new IntBufferWrapped(ints);
		}
	}
	/**
	 * This object is replaces itself with the original type of object after it is
	 * de-serialized.
	 * @author rizsi
	 *
	 */
	static class IntBufferWrapped implements IReplaceSerializable
	{
		private static final long serialVersionUID = 1L;
		private int[] ints;
		public IntBufferWrapped(int[] ints) {
			super();
			this.ints = ints;
		}
		@Override
		public Object readResolve() {
			return IntBuffer.wrap(ints);
		}
	}
	/**
	 * Create a remoting configuration that supports a callback type
	 * and a serialization replace type.
	 * If we didn't use these features the configuration could be omitted.
	 * @return
	 */
	public static CoolRMIServiceRegistry createConfiguration() {
		CoolRMIServiceRegistry reg=new CoolRMIServiceRegistry();
		// Proxy type has to be configured on the side that sends the callback object. (On the client in our case)
		// Only exact matches of this type are proxied (this is a limitation of the current implementation)
		reg.addProxyType(CallbackImpl.class, ICallback.class);
		// Replace type has to be configured on the side that sends the replaced object. (On the client in our case)
		reg.addReplaceType(new IntBufferReplace());
		return reg;
	}
}
```

Proxy object types also must be configured but that is just a single line of code:

```
		CoolRMIServiceRegistry reg;
		reg.addProxyType(CallbackImpl.class, ICallback.class);
```

