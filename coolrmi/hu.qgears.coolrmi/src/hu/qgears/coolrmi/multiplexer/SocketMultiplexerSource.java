package hu.qgears.coolrmi.multiplexer;

import java.io.ByteArrayInputStream;

/**
 * A datagram source that must be sent.
 * @author rizsi
 *
 */
public class SocketMultiplexerSource {
	private long id;
	public long getId() {
		return id;
	}
	public ByteArrayInputStream getToSend() {
		return toSend;
	}
	public SocketMultiplexerSource(long id, ByteArrayInputStream toSend) {
		super();
		this.id = id;
		this.toSend = toSend;
	}
	private ByteArrayInputStream toSend;
}
