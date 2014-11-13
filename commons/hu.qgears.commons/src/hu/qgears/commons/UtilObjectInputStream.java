package hu.qgears.commons;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Object input stream with explicitly set classLoader for deserialization.
 * @author rizsi
 *
 */
public class UtilObjectInputStream extends ObjectInputStream{
	ClassLoader classLoader;
	ClassLoader clientClassLoader;
	public UtilObjectInputStream(
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
