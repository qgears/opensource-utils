package hu.qgears.coolrmi.test.bigmemservice;

/**
 * Simple service that can be used to drive the message serialization subsystem with "big" messages.
 */
public interface IBigMemService {
	public static final String id="bigmemservice";

	byte[] sendReceive(byte[] bs);
}
