package hu.qgears.commons;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.log4j.Logger;

/**
 * Helper class for accessing Java nio channels.
 * @author rizsi
 *
 */
public class UtilChannel {
	
	private static final Logger LOG = Logger.getLogger(UtilChannel.class);
	
	private UtilChannel() {
		//ctor of utility class
	}
	
	/**
	 * Read N bytes from a channel into the beginning of a byte buffer
	 * @param channel should be in blocking mode
	 * @param target
	 * @param n
	 * @throws IOException
	 */
	public static final void readNBytes(ReadableByteChannel channel,
			ByteBuffer target, int n) throws IOException
	{
		target.clear();
		target.limit(n);
		while(n>0)
		{
			int k=channel.read(target);
			if(k<0)
			{
				throw new EOFException("Channel reached EOF");
			}else if(k==0)
			{
				// Non-blocking channel?
				try {
					Thread.sleep(0, 10000);
				} catch (InterruptedException e) {
					LOG.error("ReadNBytes",e);
				}
			}
			else
			{
				n-=k;
			}
		}
	}
	public static void writeNBytes(WritableByteChannel channel, ByteBuffer bb) throws IOException {
		while(bb.hasRemaining())
		{
			int n=channel.write(bb);
			if(n==0)
			{
				// Channel filled up. Don't 100% CPU!
				try {
					Thread.sleep(0, 1000000);
				} catch (InterruptedException e) {
					LOG.error("WriteNBytes",e);
				}
			}
		}
	}
}
