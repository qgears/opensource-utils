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
 * SHA1 hash helper utilities.
 */
public class UtilSha1 {
	/**
	 * Prevent instantiation.
	 */
	private UtilSha1(){}
	/**
	 * Length of an SHA-1 hash Hex format in characters.
	 */
	public static final int HASHLENGTH=40;
	public static String getSha1(String f)
	{
		try {
			MessageDigest m = MessageDigest.getInstance("SHA1");
			m.update(f.getBytes(StandardCharsets.UTF_8));
			return toSha1String(m);
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}
	}
	public static byte[] getSha1Bytes(String f)
	{
		try {
			MessageDigest m = MessageDigest.getInstance("SHA1");
			m.update(f.getBytes(StandardCharsets.UTF_8));
			return m.digest();
		} catch (NoSuchAlgorithmException e) {
			// Never happens as MD5 is part of JVM
			throw new RuntimeException(e);
		}
	}
	public static String getSha1(byte[] f)
	{
		try {
			MessageDigest m = MessageDigest.getInstance("SHA1");
			m.update(f);
			return toSha1String(m);
		} catch (NoSuchAlgorithmException e) {
			// Never happens as SHA1 is part of JVM
			throw new RuntimeException(e);
		}
	}
	public static String getSha1(File f) throws FileNotFoundException, IOException
	{
		try {
			MessageDigest m = MessageDigest.getInstance("SHA1");
			byte[] b=new byte[UtilFile.defaultBufferSize.get()];
			try(FileInputStream fis=new FileInputStream(f))
			{
				int len=fis.read(b);
				while(len>0)
				{
					m.update(b, 0, len);
					len=fis.read(b);
				}
			}
			return toSha1String(m);
		} catch (NoSuchAlgorithmException e) {
			// Never happens as SHA1 is part of JVM
			throw new RuntimeException(e);
		}
	}
	/**
	 * Create an sha1 hash digest. Exceptions are transformed to {@link RuntimeException} inside.
	 * @return
	 */
	public static MessageDigest createSha1()
	{
		try {
			return MessageDigest.getInstance("SHA1");
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
	public static String toSha1String(MessageDigest m)
	{
		return UtilString.padLeft(new BigInteger(1, m.digest()).toString(16), HASHLENGTH, '0');
	}
}
