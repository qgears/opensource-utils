package hu.qgears.coolrmi.streams;

import java.io.IOException;

public interface IConnectionServer {

	void close() throws IOException;

	IConnection accept() throws IOException;

}
