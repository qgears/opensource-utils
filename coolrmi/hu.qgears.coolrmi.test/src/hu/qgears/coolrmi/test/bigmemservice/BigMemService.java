package hu.qgears.coolrmi.test.bigmemservice;

/**
 * Simple service that can be used to drive the message serialization subsystem with "big" messages.
 */
public class BigMemService implements IBigMemService
{

	@Override
	public byte[] sendReceive(byte[] bs) {
		return bs;
	}

}
