package hu.qgears.images.test;

import java.io.IOException;
import java.net.InetSocketAddress;

import hu.qgears.images.SizeInt;
import hu.qgears.images.vnc.VNCServer;

public class ManualTestVNCServer {
	public static void main(String[] args) throws IOException {
		new VNCServer(new SizeInt(640, 480)).start(new InetSocketAddress("localhost", 5009), false);
	}
}
