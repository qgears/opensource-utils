package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class JavaPipeServer implements IConnectionServer {
	
	private LinkedBlockingQueue<JavaPipeConnection> servers=new LinkedBlockingQueue<JavaPipeConnection>();
	@Override
	public void close() throws IOException {
	}

	@Override
	public IConnection accept() throws IOException {
		try {
			return servers.take();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	public IConnection clientConnect() throws IOException {
		PipedInputStream cis=new PipedInputStream();
		PipedOutputStream sos=new PipedOutputStream();
		PipedInputStream sis=new PipedInputStream();
		PipedOutputStream cos=new PipedOutputStream();
		sis.connect(cos);
		sos.connect(cis);
		JavaPipeConnection c=new JavaPipeConnection(cis, cos);
		JavaPipeConnection s=new JavaPipeConnection(sis, sos);
		servers.add(s);
		return c;
	}

}
