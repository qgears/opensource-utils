package hu.qgears.coolrmi.multiplexer;

import java.io.ByteArrayInputStream;

import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;

/**
 * A datagram source that must be sent.
 * @author rizsi
 *
 */
public class SocketMultiplexerSource {
	private long id;
	private ByteArrayInputStream toSend;
	private AbstractCoolRMIMessage message;
	public long getId() {
		return id;
	}
	public ByteArrayInputStream getToSend() {
		return toSend;
	}
	public SocketMultiplexerSource(long id, ByteArrayInputStream toSend, AbstractCoolRMIMessage message) {
		super();
		this.id = id;
		this.toSend = toSend;
		this.message=message;
	}
	@Override
	public String toString() {
		return "Socket multiplexer source: "+message.getName();
	}
	/**
	 * Callback when the last piece of this message has been sent through the (TCP) channel.
	 */
	public void sent() {
		message.sent();
	}
}
