package hu.qgears.remote;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import hu.qgears.commons.UtilFile;

public class StreamSender extends Thread {
	InputStream is;
	ICallback cb;
	/**
	 * Large buffer is used because the sync nature of cb.data() remote call introduces a huge latency.
	 */
	private int bufferSize=1024*1024;
	
	public StreamSender(InputStream is, ICallback cb) throws IOException {
		super("StreamSender1");
		this.is = is;
		this.cb = cb;
		pis=new PipedInputStream(bufferSize);
		pos=new PipedOutputStream(pis);
		setDaemon(true);
	}
	@Override
	public synchronized void start() {
		super.start();
		Thread th2=new Thread("StreamSender2")
		{
			@Override
			public void run() {
				run2();
			}
		};
		th2.setDaemon(true);
		th2.start();
	}
	PipedInputStream pis;
	PipedOutputStream pos;
	@Override
	public void run() {
		byte[] buffer=new byte[UtilFile.defaultBufferSize.get()];
		try {
			while(true)
			{
				int n=is.read(buffer);
				if(n>0)
				{
					pos.write(buffer, 0,  n);
				}
				else
				{
					System.out.println("EOF");
					throw new EOFException();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}finally {
			System.out.println("EOFDONE");
			try {
				pos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void run2()
	{
		byte[] buffer=new byte[bufferSize];
		try {
			while(true)
			{
				int n=pis.read(buffer);
				if(n>0)
				{
					cb.data(Arrays.copyOf(buffer, n));
				}
				else
				{
					throw new EOFException();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}finally
		{
			cb.close();
		}
	}
}
