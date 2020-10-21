package hu.qgears.commons;

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
			return UtilString.padLeft(new BigInteger(1, m.digest()).toString(16), HASHLENGTH, '0');
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
			return UtilString.padLeft(new BigInteger(1, m.digest()).toString(16), HASHLENGTH, '0');
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
		return UtilString.padLeft(new BigInteger(1, m.digest()).toString(16), HASHLENGTH, '0');
	}
}
