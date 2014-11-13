package hu.qgears.coolrmi.multiplexer;

import java.io.Serializable;

/**
 * Piece of a message on a multiplexed
 * message stream.
 * @author rizsi
 *
 */
public class SocketMultiplexerDatagram implements Serializable {
	private static final long serialVersionUID = 1L;
	long datagramId;
	public long getDatagramId() {
		return datagramId;
	}
	public void setDatagramId(long datagramId) {
		this.datagramId = datagramId;
	}
	public boolean isLastPiece() {
		return lastPiece;
	}
	public void setLastPiece(boolean lastPiece) {
		this.lastPiece = lastPiece;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	boolean lastPiece;
	byte[] content;
	@Override
	public String toString() {
		return "datagram: "+datagramId+" "+content.length+" "+lastPiece;
	}
}
