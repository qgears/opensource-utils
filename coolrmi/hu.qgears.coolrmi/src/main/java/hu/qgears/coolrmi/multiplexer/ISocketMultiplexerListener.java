package hu.qgears.coolrmi.multiplexer;

/**
 * Listening endpoint of a data channel which receives 
 * whole messages as a byte array.
 */
public interface ISocketMultiplexerListener {
	public void messageReceived(byte[] msg);

	public void pipeBroken(Exception e);
}
