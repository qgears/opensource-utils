package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer implements IConnectionServer
{
	private ServerSocket sock;

	public TCPServer(ServerSocket sock) {
		super();
		this.sock = sock;
	}

	@Override
	public void close() throws IOException {
		sock.close();
	}

	@Override
	public IConnection accept() throws IOException {
		return new TCPConnection(sock.accept());
	}
	
}
