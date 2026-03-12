package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hu.qgears.commons.NoExceptionAutoClosable;

public interface IConnection extends NoExceptionAutoClosable {

	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	ConnectionConfiguration getConfiguration();
}
