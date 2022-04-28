package hu.qgears.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Connect two streams to each other by a thread that reads input and writes to 
 * output at once.
 * @author rizsi
 *
 */
public class ConnectStreams extends Thread {
	private static final Logger LOG = Logger.getLogger(ConnectStreams.class);
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
		doStream(is, os, closeOs, UtilFile.defaultBufferSize.get());
	}
	/**
	 * Copy all data from input stream to the output stream using this thread.
	 * Buffer size is the default specified in {@link UtilFile}
	 * Target is not closed after consuming input.
	 * @param source data is read from this stream
	 * @param target data is written to this stream
	 * @throws IOException passed up from {@link #doStream(InputStream, OutputStream, boolean, int)}
	 */
	public static void doStream(final InputStream source,
			final OutputStream target) throws IOException {
		doStream(source, target, false, UtilFile.defaultBufferSize.get());
	}

	/**
	 * Copy all data from input stream to the output stream using this thread.
	 * @param source
	 * @param target
	 * @param closeOutput target is closed after input was consumed if true
	 * @param bufferSize size of the buffer used when copying
	 * @throws IOException
	 */
	public static void doStream(final InputStream source,
			final OutputStream target, boolean closeOutput, int bufferSize) throws IOException {
		doStream(source, target, closeOutput, bufferSize, true);
	}
	/**
	 * Copy all data from input stream to the output stream using this thread.
	 * @param source
	 * @param target
	 * @param closeOutput target is closed after input was consumed if true
	 * @param bufferSize size of the buffer used when copying
	 * @param flushEachIteration When set to true then each iteration calls a flush on the target. Useful when real time streams are connected.
	 * 			Should be set to false when it is only used to copy data without real time requirement.
	 * @throws IOException
	 */
	public static void doStream(final InputStream source,
			final OutputStream target, boolean closeOutput, int bufferSize, boolean flushEachIteration) throws IOException {
		try
		{
			try {
				int n;
				byte[] cbuf = new byte[bufferSize];
				while ((n = source.read(cbuf)) > -1) {
					target.write(cbuf, 0, n);
					if(flushEachIteration)
					{
						target.flush();
					}
				}
			} finally {
				if(source!=null)
				{
					source.close();
				}
			}
		}finally
		{
			if(closeOutput&&target!=null)
			{
				target.close();
			}
		}
	}
	/**
	 * Start a new thread that streams data from input to output.
	 * @param is
	 * @param os
	 * @return New thread that copies data from is to os. Thread exits when the source or target is closed. 
	 */
	public static Thread startStreamThread(final InputStream is, final OutputStream os)
	{
		Thread ret=new Thread() {
			public void run() {
				try {
					ConnectStreams.doStream(is, os);
				} catch (IOException e) {
					LOG.error("Exception during streaming error stream", e);
				}
			};
		};
		ret.start();
		return ret;
	}
	/**
	 * Start a new thread that streams data from input to output.
	 * @param is
	 * @param os
	 * @param closeOutput target is closed after input was consumed if true
	 * @param bufferSize size of the buffer used when copying
	 * @return New thread that copies data from is to os. Thread exits when the source or target is closed.
	 */
	public static Thread startStreamThread(final InputStream is, final OutputStream os, final boolean closeOutput, final int bufferSize)
	{
		Thread ret=new Thread() {
			public void run() {
				try {
					ConnectStreams.doStream(is, os, closeOutput, bufferSize);
				} catch (IOException e) {
					LOG.error("Exception during streaming error stream", e);
				}
			};
		};
		ret.start();
		return ret;
	}
}
