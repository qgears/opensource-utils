package hu.qgears.coolrmi.multiplexer;

import java.io.ByteArrayInputStream;

/**
 * A datagram source that must be sent.
 * @author rizsi
 *
 */
public class SocketMultiplexerSource {
	private long id;
	private ByteArrayInputStream toSend;
	private String name;
	public long getId() {
		return id;
	}
	public ByteArrayInputStream getToSend() {
		return toSend;
	}
	public SocketMultiplexerSource(long id, ByteArrayInputStream toSend, String name) {
		super();
		this.id = id;
		this.toSend = toSend;
		this.name=name;
	}
	@Override
	public String toString() {
		return "Socket multiplexer source: "+name;
	}
}
