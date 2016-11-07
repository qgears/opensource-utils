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

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;
import hu.qgears.coolrmi.streams.IConnection;



/**
 * Class for internal use.
 * This class executes a processing thread for each client connected to the server. 
 * @author rizsi
 *
 */
public class CoolRMIServe
	extends CoolRMIRemoter{
	CoolRMIServer coolRMIServer;
	IConnection sock;
	protected CoolRMIServe(CoolRMIServer coolRMIServer,
			IConnection sock, boolean guaranteeOrdering) throws IOException {
		super(coolRMIServer.getClassLoader(), guaranteeOrdering);
		this.sock=sock;
		this.coolRMIServer = coolRMIServer;
	}

	public void connect() throws IOException {
		super.connect(sock);
	}
}
