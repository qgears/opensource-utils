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
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Open input stream, output stream and object serialization streams over them
 * for a given socket.
 * @author rizsi
 *
 */
public class CoolRMISocketStreams {
	ClassLoader classLoader;
	InputStream is;
	OutputStream os;
	private CoolRMIObjectInputStream oin;
	private ObjectOutputStream oos;

	public ObjectOutputStream getOos() {
		return oos;
	}

	public CoolRMISocketStreams(ClassLoader classLoader, Socket sock) throws IOException {
		is = sock.getInputStream();
		os = sock.getOutputStream();
		oos = new ObjectOutputStream(os);
		this.classLoader=classLoader;
	}

	/**
	 * Object input stream is lazily initiated on first use because it blocks
	 * the thread until the first object is sent.
	 * @return Input object stream
	 * @throws IOException
	 */
	public CoolRMIObjectInputStream getOin() throws IOException {
		if(oin==null)
		{
			oin = new CoolRMIObjectInputStream(classLoader, is);
		}
		return oin;
	}
	
}
