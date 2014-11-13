package hu.qgears.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Connect two streams to each other by a thread that reads input and writes to 
 * output at once.
 * @author rizsi
 *
 */
public class ConnectStreams extends Thread {
	private InputStream is;
	private OutputStream os;
	private PrintStream err=System.err;
	private boolean closeOs=true;
	public boolean isCloseOs() {
		return closeOs;
	}
	public void setCloseOs(boolean closeOs) {
		this.closeOs = closeOs;
	}
	public void setErr(PrintStream err) {
		this.err = err;
	}
	public ConnectStreams(InputStream is, OutputStream os) {
		super();
		this.is = is;
		this.os = os;
	}
	@Override
	public void run()
	{
		try {
			runConnect();
		} catch (IOException e) {
			if(err!=null)
			{
				e.printStackTrace(err);
			}
		}
	}
	public void close()
	{
		try {
			is.close();
		} catch (IOException e) {
			if(err!=null)
			{
				e.printStackTrace(err);
			}
		}
	}
	public void runConnect() throws IOException
	{
		byte[] buffer=new byte[1024];
		int c;
		while((c=is.read(buffer))>=0)
		{
			os.write(buffer, 0, c);
			os.flush();
		}
		is.close();
		if(closeOs)
		{
			os.close();
		}
	}
}
