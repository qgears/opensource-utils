package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPConnection implements IConnection
{
	private Socket sock;
	
	public TCPConnection(Socket sock) {
		super();
		this.sock = sock;
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
	public void close() throws IOException {
		sock.close();
	}

}
