package hu.qgears.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Md5 hash helper utilities.
 */
public class UtilMd5 {
	
	/**
	 * Prevent instantiation.
	 */
	private UtilMd5(){}
	/**
	 * Length of an MD5 hash Hex format in characters.
	 */
	public static final int HASHLENGTH=32;
	/**
	 * Md5 hash the string (transformed to binary using UTF-8)
	 * @param f
	 * @return
	 */
	public static String getMd5(String f)
	{
		try {
			// Cryptgraphic hash is not required here, but specifically MD5 is 
			@SuppressWarnings("squid:S2070")
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(f.getBytes(StandardCharsets.UTF_8));
			return toMd5String(m);
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}

	}
	/**
	 * Md5 hash the data (transformed to binary using UTF-8)
	 * @param data
	 * @return
	 */
	public static byte[] getMd5Bytes(byte[] data)
	{
		try {
			// Cryptgraphic hash is not required here, but specifically MD5 is 
			@SuppressWarnings("squid:S2070")
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(data);
			return m.digest();
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}

	}
	/**
	 * MD5 hash of the 'file'.
	 * @param f
	 * @return
	 */
	public static String getMd5(byte[] f)
	{
		try {
			// Cryptgraphic hash is not required here, but specifically MD5 is 
			@SuppressWarnings("squid:S2070")
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(f);
			return toMd5String(m);
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}
	}
	/**
	 * MD5 hash of the 'file'.
	 * @param f file to be opened and hash counted
	 * @return md5 hash of the file's content in canonical string format
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static String getMd5(File f) throws IOException
	{
		try {
			byte[] buffer=new byte[UtilFile.defaultBufferSize.get()];
			// Cryptgraphic hash is not required here, but specifically MD5 is 
			@SuppressWarnings("squid:S2070")
			MessageDigest m = MessageDigest.getInstance("MD5");
			try(FileInputStream fis=new FileInputStream(f))
			{
				while(true)
				{
					int n=fis.read(buffer);
					if(n<1)
					{
						break;
					}
					m.update(buffer, 0, n);
				}
			}
			return toMd5String(m);
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}
	}
	/**
	 * Create an md5 hash digest. Exceptions are transformed to {@link RuntimeException} inside.
	 * @return
	 */
	// Cryptgraphic hash is not required here, but specifically MD5 is 
	@SuppressWarnings("squid:S2070")
	public static MessageDigest createMd5()
	{
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}
	}
	/**
	 * Finish the digest and convert to String.
	 * @param m
	 * @return
	 */
	public static String toMd5String(MessageDigest m)
	{
		return md5SumByteArrayToString(m.digest());
	}
	/**
	 * Convert an md5sum digest result to string.
	 * @return
	 */
	public static String md5SumByteArrayToString(byte[] digest)
	{
		return UtilString.padLeft(new BigInteger(1, digest).toString(16), HASHLENGTH, '0');
	}
}
