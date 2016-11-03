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

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;



/**
 * RMI server object. Can be used to create a Cool RMI server,
 * add services and open a port to listen to.
 * @author rizsi
 *
 */
public class CoolRMIServer {
	private CoolRMIServiceRegistry serviceRegistry=new CoolRMIServiceRegistry();
	private ClassLoader classLoader;
	private SocketAddress socketAddress;
	private boolean guaranteeOrdering;
	private long timeout=30000;
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
	 * @param classLoader Classloader for all object serialization. The classloader must see all communication interfaces and all CoolRMI classes.
	 * @param host host name or IP to bind server on
	 * @param port port to bind server to.
	 */
	public CoolRMIServer(ClassLoader classLoader,
			SocketAddress socketAddress,
			boolean guaranteeOrdering) {
		this.classLoader=classLoader;
		this.socketAddress=socketAddress;
		this.guaranteeOrdering=guaranteeOrdering;
	}

	ServerSocket socket;
	boolean exit = false;
	/**
	 * Start listening for clients.
	 * @throws IOException 
	 */
	public void start() throws IOException {
		socket = new ServerSocket();
		socket.bind(socketAddress);
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
					Socket sock = socket.accept();
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
