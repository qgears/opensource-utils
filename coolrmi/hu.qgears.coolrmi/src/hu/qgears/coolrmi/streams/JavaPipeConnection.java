package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * An object that stores the two ends of a piped stream
 */
public class JavaPipeConnection implements IConnection{
	private PipedInputStream is;
	private PipedOutputStream os;
	public JavaPipeConnection(PipedInputStream is, PipedOutputStream os) {
		this.is=is;
		this.os=os;
	}
	/**
	 * Create a Java pipe connection based on a new instance of an input and an output stream already connected.
	 * @throws IOException
	 */
	public JavaPipeConnection() throws IOException {
		this.is=new PipedInputStream();
		this.os=new PipedOutputStream(is);
	}

	@Override
	public InputStream getInputStream() {
		return is;
	}

	@Override
	public OutputStream getOutputStream() {
		return os;
	}

	@Override
	public void close() throws IOException {
		is.close();
		os.close();
	}
}
