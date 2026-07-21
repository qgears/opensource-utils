package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class TCPConnection implements IConnection
{
	private Socket sock;
	private ConnectionConfiguration configuration=new ConnectionConfiguration();
	public TCPConnection(Socket sock) throws SocketException {
		super();
		this.sock = sock;
		sock.setTcpNoDelay(true);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return sock.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return sock.getOutputStream();
	}

	@Override
	public void close() {
		try {
			sock.close();
		} catch (IOException e) {
			configuration.getLog().logError(e);
		}
	}
	@Override
	public ConnectionConfiguration getConfiguration() {
		return configuration;
	}
}
