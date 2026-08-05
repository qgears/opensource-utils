package hu.qgears.emfcollab.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Serialize Java obejcts to bytes and deserialize then to objects.
 * @author rizsi
 *
 */
public class UtilSerializator {
	
	
	private UtilSerializator() {
		// disable constructor of utility class
	}
	
	/**
	 * Serialize the object to an array of bytes
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(Object o) throws IOException
	{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		oos.writeObject(o);
		oos.close();
		return bos.toByteArray();
	}
	/**
	 * Deserialize the object from an array of bytes
	 * @param bs
	 * @param classLoader
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserialize(byte[] bs, ClassLoader classLoader) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis=new ByteArrayInputStream(bs);
		UtilObjectInputStream ois=new UtilObjectInputStream(classLoader, bis);
		try {
			return ois.readObject();
		} finally{
			ois.close();
		}
	}
}
