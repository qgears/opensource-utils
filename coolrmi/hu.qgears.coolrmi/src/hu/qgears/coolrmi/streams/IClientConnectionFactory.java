package hu.qgears.coolrmi.streams;

import java.io.IOException;

public interface IClientConnectionFactory {

	IConnection connect() throws IOException;

}
