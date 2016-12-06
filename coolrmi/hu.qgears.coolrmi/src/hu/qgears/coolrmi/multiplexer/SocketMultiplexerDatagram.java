package hu.qgears.coolrmi.multiplexer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Piece of a message on a multiplexed
 * message stream.
 * @author rizsi
 *
 */
public class SocketMultiplexerDatagram implements Serializable {
	private static final long serialVersionUID = 1L;
	private long datagramId;
	private boolean lastPiece;
	private byte[] content;
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
	public void setContent(byte[] content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "datagram: "+datagramId+" "+content.length+" "+lastPiece;
	}
	public static SocketMultiplexerDatagram readFromStream(InputStream is) throws IOException {
		byte[] header=new byte[8+4+1];
		readAll(is, header);
		ByteBuffer bb=ByteBuffer.wrap(header);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		long datagramId=bb.getLong();
		int length=bb.getInt();
		boolean lastPiece=bb.get()!=0;
		byte[] data=new byte[length];
		readAll(is,  data);
		return new SocketMultiplexerDatagram(datagramId, data, lastPiece);
	}
	private static void readAll(InputStream is, byte[] header) throws IOException {
		int at=0;
		while(at<header.length)
		{
			int n=is.read(header, at, header.length-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}
	public void writeToStream(OutputStream os) throws IOException {
		byte[] header=new byte[8+4+1];
		ByteBuffer bb=ByteBuffer.wrap(header);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong(datagramId);
		bb.putInt(content.length);
		bb.put(lastPiece?(byte)1:(byte)0);
		os.write(header);
		os.write(content);
	}
}
