package hu.qgears.coolrmi.multiplexer;

public interface ISocketMultiplexerListener {
	public void messageReceived(byte[] msg);

	public void pipeBroken(Exception e);
}
