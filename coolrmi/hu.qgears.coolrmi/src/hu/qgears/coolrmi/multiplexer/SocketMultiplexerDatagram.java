package hu.qgears.coolrmi.multiplexer;

import java.io.Serializable;

/**
 * Piece of a message on a multiplexed message stream.
 * @author rizsi
 *
 */
public class SocketMultiplexerDatagram implements Serializable {
	private static final long serialVersionUID = 1L;
	protected long datagramId;
	protected boolean lastPiece;
	protected byte[] content;
	public static final long DATAGRAM_MAGIC=0xDAFABCA6ABBACD34l;
	public static final long DATAGRAM_MAGIC_END=0xDAFABCA6ABBACD35l;
	public SocketMultiplexerDatagram(long datagramId, byte[] data, boolean lastPiece) {
		this.datagramId=datagramId;
		this.content=data;
		this.lastPiece=lastPiece;
	}
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
	@Override
	public String toString() {
		return "datagram: "+datagramId+" "+content.length+" "+lastPiece;
	}
}
