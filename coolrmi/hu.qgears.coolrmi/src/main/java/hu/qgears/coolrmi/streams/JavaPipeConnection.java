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
	protected ConnectionConfiguration configuration=new ConnectionConfiguration();
	public JavaPipeConnection() throws IOException {
		is=new PipedInputStream();
		os=new PipedOutputStream();
		is.connect(os);
		os.connect(is);
	}
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
	public void close() {
		try {
			is.close();
		} catch (IOException e) {
			configuration.getLog().logError(e);
		}
		try {
			os.close();
		} catch (IOException e) {
			configuration.getLog().logError(e);
		}
	}
	@Override
	public ConnectionConfiguration getConfiguration() {
		return configuration;
	}
}
