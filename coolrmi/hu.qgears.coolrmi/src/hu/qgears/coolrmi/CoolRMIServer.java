/**
 *  Copyright CoolRMI Schmidt András

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package hu.qgears.coolrmi;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import hu.qgears.coolrmi.streams.IConnection;
import hu.qgears.coolrmi.streams.IConnectionServer;
import hu.qgears.coolrmi.streams.IConnectionServerFactory;
import hu.qgears.coolrmi.streams.TCPServerFactory;



/**
 * RMI server object. Can be used to create a Cool RMI server,
 * add services and open a port to listen to.
 * @author rizsi
 *
 */
public class CoolRMIServer {
	private CoolRMIServiceRegistry serviceRegistry=new CoolRMIServiceRegistry();
	private ClassLoader classLoader;
	private IConnectionServerFactory serverFactory;
	private IConnectionServer socket;
	private boolean guaranteeOrdering;
	private long timeout=30000;
	private boolean exit = false;
	public CoolRMIServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(CoolRMIServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public long getTimeout() {
		return timeout;
	}

	/**
	 * Must be set before starting the server!
	 * @param timeout
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Create a new RMI server.
	 * The server must have services added and then started after creation
	 * @param classLoader {@link ClassLoader} to use for serializing communication messages. Must see all of CoolRMI and the communication interfaces.
	 * @param serverFactory factory to create a server connection (eg TCP server socket)
	 * @param guaranteeOrdering guarantee that the ordering of method calls is same on the client side as on the server side.
	 */
	public CoolRMIServer(ClassLoader classLoader,
			IConnectionServerFactory serverFactory,
			boolean guaranteeOrdering) {
		this.classLoader=classLoader;
		this.serverFactory=serverFactory;
		this.guaranteeOrdering=guaranteeOrdering;
	}
	/**
	 * Create a new RMI server.
	 * The server must have services added and then started after creation
	 * @param classLoader {@link ClassLoader} to use for serializing communication messages. Must see all of CoolRMI and the communication interfaces.
	 * @param socketAddress Server socket address to bind to when starting the server.
	 * @param guaranteeOrdering guarantee that the ordering of method calls is same on the client side as on the server side.
	 */
	public CoolRMIServer(ClassLoader classLoader,
			SocketAddress socketAddress,
			boolean guaranteeOrdering) {
		this.classLoader=classLoader;
		this.serverFactory=new TCPServerFactory(socketAddress);
		this.guaranteeOrdering=guaranteeOrdering;
	}

	/**
	 * Start listening for clients.
	 * @throws IOException 
	 */
	public void start() throws IOException {
		socket=serverFactory.bindServer();
		Thread th = new Thread(new Runnable(){
			public void run() {
				CoolRMIServer.this.run();
			}}, "CoolRMIServer");
		th.start();
	}

	/**
	 * Close RMI server.
	 * This will free all resources.
	 * @throws IOException
	 */
	public void close() throws IOException {
		exit = true;
		socket.close();
	}

	private void run() {
		try {
			try {
				while (!exit) {
					IConnection sock = socket.accept();
					CoolRMIServe serve=
						new CoolRMIServe(this, sock, guaranteeOrdering);
					serve.setTimeout(timeout);
					serve.setServiceRegistry(getServiceRegistry());
					serve.connect();
				}
			} finally {
				socket.close();
			}
		} catch (SocketException e)
		{
			if(!exit)
			{
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
	}
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
