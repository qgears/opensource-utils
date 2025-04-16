package hu.qgears.coolrmi.streams;

import java.io.IOException;

public class JavaPipeServerFactory implements IConnectionServerFactory {
	private JavaPipeServer server;
	
	public JavaPipeServerFactory(JavaPipeServer server) {
		super();
		this.server = server;
	}

	@Override
	public IConnectionServer bindServer() throws IOException {
		return server;
	}

}
