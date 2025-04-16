package hu.qgears.coolrmi.streams;

import java.io.IOException;

public interface IConnectionServerFactory {

	IConnectionServer bindServer() throws IOException;

}
