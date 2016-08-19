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
