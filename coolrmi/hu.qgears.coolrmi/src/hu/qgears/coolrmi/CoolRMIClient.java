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

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;



/**
 * Cool RMI client.
 * Can be used to create a client object that implements
 * the user defined communication interface and connects to the
 * specified server on method call.
 * @author rizsi
 *
 */
public class CoolRMIClient extends CoolRMIRemoter {
	private SocketAddress socketAddress;
	/**
	 * Create a client object that is parametered with server's
	 * TCP address.
	 * @param host
	 * @param port
	 * @throws IOException 
	 */
	public CoolRMIClient(ClassLoader classLoader,
			SocketAddress socketAddress,
			boolean guaranteeOrdering) throws IOException {
		super(classLoader, guaranteeOrdering);
		this.socketAddress=socketAddress;
		connect();
	}
	private void connect() throws IOException
	{
		Socket socket = new Socket();
		socket.connect(socketAddress);
		super.connect(socket);
	}
}
