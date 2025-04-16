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
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Object input stream with explicitly set classLoader for deserialization.
 * @author rizsi
 *
 */
public class CoolRMIObjectInputStream extends ObjectInputStream{
	ClassLoader classLoader;
	ClassLoader clientClassLoader;
	public CoolRMIObjectInputStream(
			ClassLoader classLoader, java.io.InputStream is) throws IOException
	{
		super(is);
		this.classLoader=classLoader;
	}
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		if(clientClassLoader!=null)
		{
			try
			{
				return Class.forName(desc.getName(), true, clientClassLoader);
			}catch(ClassNotFoundException e){}
		}
		return Class.forName(desc.getName(), true, classLoader);
	}
	public ClassLoader getClientClassLoader() {
		return clientClassLoader;
	}
	public void setClientClassLoader(ClassLoader clientClassLoader) {
		this.clientClassLoader = clientClassLoader;
	}
	/**
	 * Read an object using the given class loader as primary classloader.
	 * @param classLoader
	 * @return 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public Object readObject(ClassLoader classLoader) throws IOException, ClassNotFoundException {
		clientClassLoader=classLoader;
		try
		{
			return readObject();
		}finally
		{
			clientClassLoader=null;
		}
	}
}
