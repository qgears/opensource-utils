package hu.qgears.commons;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream implementation that passes input to two different streams.
 */
public class StreamTee extends OutputStream {
	OutputStream o1;
	OutputStream o2;
	boolean close1;
	boolean close2;
	
	public StreamTee(OutputStream o1, boolean close1, OutputStream o2, boolean close2) {
		super();
		this.o1 = o1;
		this.close1 = close1;
		this.o2 = o2;
		this.close2 = close2;
	}
	@Override
	public void write(int b) throws IOException {
		o1.write(b);
		o2.write(b);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		o1.write(b, off, len);
		o2.write(b, off, len);
	}
	@Override
	public void write(byte[] b) throws IOException {
		o1.write(b);
		o2.write(b);
	}
	@Override
	public void close() throws IOException {
		if(close1)
		{
			o1.close();
		}else
		{
			o1.flush();
		}
		if(close2)
		{
			o2.close();
		}else
		{
			o2.flush();
		}
	}
}
