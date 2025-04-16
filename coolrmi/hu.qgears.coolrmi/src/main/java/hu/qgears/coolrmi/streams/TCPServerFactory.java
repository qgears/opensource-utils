package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;

public class TCPServerFactory implements IConnectionServerFactory {
	SocketAddress socketAddress;
	public TCPServerFactory(SocketAddress socketAddress) {
		this.socketAddress=socketAddress;
	}
	@Override
	public IConnectionServer bindServer() throws IOException {
		ServerSocket socket = new ServerSocket();
		socket.bind(socketAddress);
		return new TCPServer(socket);
	}

}
