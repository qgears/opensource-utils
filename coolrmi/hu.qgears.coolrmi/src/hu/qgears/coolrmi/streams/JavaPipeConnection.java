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

	
	public JavaPipeConnection() throws IOException {
		is=new PipedInputStream();
		os=new PipedOutputStream();
		is.connect(os);
		os.connect(is);
	}

	/**
	 * Create a Java pipe connection based on a new instance of an input and an output stream already connected.
	 * @throws IOException
	 */
	public JavaPipeConnection(PipedInputStream is, PipedOutputStream os) {
		this.is=is;
		this.os=os;
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
