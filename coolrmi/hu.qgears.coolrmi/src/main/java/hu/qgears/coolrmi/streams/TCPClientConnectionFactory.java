package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPClientConnectionFactory implements IClientConnectionFactory{
	private SocketAddress socketAddress;
	
	public TCPClientConnectionFactory(SocketAddress socketAddress) {
		super();
		this.socketAddress = socketAddress;
	}

	@Override
	public IConnection connect() throws IOException {
		Socket socket = new Socket();
		socket.connect(socketAddress);
		return new TCPConnection(socket);
	}

}
