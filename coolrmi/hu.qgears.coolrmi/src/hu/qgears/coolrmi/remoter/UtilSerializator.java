package hu.qgears.coolrmi.remoter;

import hu.qgears.coolrmi.CoolRMIObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;



public class UtilSerializator {
	public static byte[] serialize(Object o) throws IOException
	{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		oos.writeObject(o);
		oos.close();
		return bos.toByteArray();
	}
	public static Object deserialize(byte[] bs, ClassLoader classLoader) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis=new ByteArrayInputStream(bs);
		CoolRMIObjectInputStream ois=new CoolRMIObjectInputStream(classLoader, bis);
		try {
			return ois.readObject();
		} finally{
			ois.close();
		}
	}
}
