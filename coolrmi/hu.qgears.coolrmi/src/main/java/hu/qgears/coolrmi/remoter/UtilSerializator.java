package hu.qgears.coolrmi.remoter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;

import hu.qgears.coolrmi.CoolRMIObjectInputStream;
import hu.qgears.coolrmi.CoolRMIObjectOutputStream;
import hu.qgears.coolrmi.messages.ISerializationErrorTransforms;


public class UtilSerializator {
	/**
	 * 
	 * @param serviceReg used to look for object replaces.
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(CoolRMIServiceRegistry serviceReg, Object o) throws IOException
	{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		CoolRMIObjectOutputStream oos=new CoolRMIObjectOutputStream(serviceReg, bos);
		if(o instanceof ISerializationErrorTransforms)
		{
			try {
				oos.writeObject(o);
				oos.close();
			} catch (ObjectStreamException e) {
				ISerializationErrorTransforms t=(ISerializationErrorTransforms)o;
				t.serializationError(e);
				bos=new ByteArrayOutputStream();
				oos=new CoolRMIObjectOutputStream(serviceReg, bos);
				oos.writeObject(o);
				oos.close();
			}
		}else
		{
			oos.writeObject(o);
			oos.close();
		}
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
