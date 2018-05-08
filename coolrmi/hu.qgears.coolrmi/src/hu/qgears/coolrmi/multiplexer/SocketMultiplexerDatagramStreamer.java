package hu.qgears.coolrmi.multiplexer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketMultiplexerDatagramStreamer {
	private byte[] sendHeader=new byte[8+8+4+1];
	private ByteBuffer sendBb=ByteBuffer.allocateDirect(sendHeader.length);
	private byte[] recvHeader=new byte[8+8+4+1];
	private ByteBuffer recvBb=ByteBuffer.allocateDirect(recvHeader.length);
	public SocketMultiplexerDatagramStreamer() {
		sendBb.order(ByteOrder.LITTLE_ENDIAN);
		recvBb.order(ByteOrder.LITTLE_ENDIAN);
	}

	public SocketMultiplexerDatagram readFromStream(InputStream is, int datagramMaxSize) throws IOException {
		readAll(is, recvHeader);
		recvBb.clear();
		recvBb.put(recvHeader);
		recvBb.flip();
		long magic=recvBb.getLong();
		if(magic!=SocketMultiplexerDatagram.DATAGRAM_MAGIC)
		{
			throw new IOException("Datagram magic signature does not match. Received value: "+Long.toHexString(magic));
		}
		long datagramId=recvBb.getLong();
		int length=recvBb.getInt();
		if(length<1 || length>datagramMaxSize)
		{
			throw new IOException("Illegal datagram size: "+length+" must be in range: [1,"+datagramMaxSize+"]");
		}
		boolean lastPiece=recvBb.get()!=0;
		byte[] data=new byte[length];
		readAll(is,  data);
		
		readAll(is, recvHeader, 8);
		recvBb.clear();
		recvBb.put(recvHeader, 0, 8);
		recvBb.flip();
		magic=recvBb.getLong();
		if(magic!=SocketMultiplexerDatagram.DATAGRAM_MAGIC_END)
		{
			throw new IOException("Datagram magic signature ending does not match. Received value: "+Long.toHexString(magic));
		}
		return new SocketMultiplexerDatagram(datagramId, data, lastPiece);
	}
	private static void readAll(InputStream is, byte[] header) throws IOException {
		readAll(is, header, header.length);
	}
	private static void readAll(InputStream is, byte[] header, int length) throws IOException {
		int at=0;
		while(at<length)
		{
			int n=is.read(header, at, length-at);
			if(n<0)
			{
				throw new EOFException();
			}
			at+=n;
		}
	}
	public void writeToStream(SocketMultiplexerDatagram d, OutputStream os) throws IOException {
		sendBb.clear();
		sendBb.putLong(SocketMultiplexerDatagram.DATAGRAM_MAGIC);
		sendBb.putLong(d.datagramId);
		sendBb.putInt(d.content.length);
		sendBb.put(d.lastPiece?(byte)1:(byte)0);
		sendBb.flip();
		sendBb.get(sendHeader);
		os.write(sendHeader);
		os.write(d.content);
		sendBb.clear();
		sendBb.putLong(SocketMultiplexerDatagram.DATAGRAM_MAGIC_END);
		sendBb.flip();
		sendBb.get(sendHeader, 0, 8);
		os.write(sendHeader, 0, 8);
	}
}
