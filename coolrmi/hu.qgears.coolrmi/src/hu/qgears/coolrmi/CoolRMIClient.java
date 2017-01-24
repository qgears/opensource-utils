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

import hu.qgears.coolrmi.messages.CoolRMIDisconnect;
import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.streams.IConnection;
import hu.qgears.coolrmi.streams.IClientConnectionFactory;
import hu.qgears.coolrmi.streams.TCPClientConnectionFactory;



/**
 * Cool RMI client.
 * Can be used to create a client object that implements
 * the user defined communication interface and connects to the
 * specified server on method call.
 * @author rizsi
 *
 */
public class CoolRMIClient extends CoolRMIRemoter {
	private IClientConnectionFactory connectionFactory;
	/**
	 * Create a client object that is parametered with server's TCP address.
	 * After creation the client is already connected on TCP.
	 * @param classLoader {@link ClassLoader} to use for serializing communication messages. Must see all of CoolRMI and the communication interfaces.
	 * @param socketAddress Socket address of the server to connect to when starting the client.
	 * @param guaranteeOrdering guarantee that the ordering of method calls is same on the client side as on the server side.
	 * @throws IOException 
	 */
	public CoolRMIClient(ClassLoader classLoader,
			SocketAddress socketAddress,
			boolean guaranteeOrdering) throws IOException {
		super(classLoader, guaranteeOrdering);
		this.connectionFactory=new TCPClientConnectionFactory(socketAddress);
		connect();
	}
	/**
	 * Create a client object that is parametered with a client connection factory.
	 * After creation the client is already connected on TCP.
	 * @param classLoader {@link ClassLoader} to use for serializing communication messages. Must see all of CoolRMI and the communication interfaces.
	 * @param connectionFactory Connection factory that can connect to the RMI server.
	 * @param guaranteeOrdering guarantee that the ordering of method calls is same on the client side as on the server side.
	 * @throws IOException 
	 */
	public CoolRMIClient(ClassLoader classLoader,
			IClientConnectionFactory connectionFactory,
			boolean guaranteeOrdering) throws IOException {
		super(classLoader, guaranteeOrdering);
		this.connectionFactory=connectionFactory;
		connect();
	}
	private void connect() throws IOException
	{
		IConnection socket=connectionFactory.connect();
		super.connect(socket);
	}
	private boolean disconnectSent=false;
	@Override
	public void close() throws IOException {
		boolean sendDisconnect;
		synchronized (this) {
			sendDisconnect=!disconnectSent;
			disconnectSent=true;
		}
		if(sendDisconnect)
		{
			CoolRMIDisconnect disconnect=new CoolRMIDisconnect();
			send(disconnect);
			disconnect.waitSent(getTimeoutMillis());
			super.close();
		}
	}
}
