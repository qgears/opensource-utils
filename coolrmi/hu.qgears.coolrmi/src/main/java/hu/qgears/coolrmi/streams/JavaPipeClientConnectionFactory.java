package hu.qgears.coolrmi.streams;

import java.io.IOException;

public class JavaPipeClientConnectionFactory implements IClientConnectionFactory{
	JavaPipeServer jps;
	public JavaPipeClientConnectionFactory(JavaPipeServer jps) {
		this.jps=jps;
	}

	@Override
	public IConnection connect() throws IOException {
		return jps.clientConnect();
	}

}
