package hu.qgears.tools.build;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public interface IFileOutput extends Closeable {

	OutputStream createOutputStream(String p, Date authorDate) throws IOException;

	/**
	 * 
	 * @return this object
	 */
	IFileOutput open();

}
