package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IConnection {

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	void close() throws IOException;

}
