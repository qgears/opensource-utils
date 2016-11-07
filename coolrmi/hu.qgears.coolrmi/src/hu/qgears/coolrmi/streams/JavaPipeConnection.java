package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class JavaPipeConnection implements IConnection{
	private PipedInputStream is;
	private PipedOutputStream os;
	public JavaPipeConnection(PipedInputStream is, PipedOutputStream os) {
		this.is=is;
		this.os=os;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return is;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return os;
	}

	@Override
	public void close() throws IOException {
		is.close();
		os.close();
	}
}
