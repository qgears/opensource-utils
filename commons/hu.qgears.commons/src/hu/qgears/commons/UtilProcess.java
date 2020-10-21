package hu.qgears.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import hu.qgears.commons.signal.SignalFutureWrapper;

/**
 * Helper methods for communicating with external processes.
 */
public class UtilProcess {

	private static final Logger LOG = Logger.getLogger(UtilProcess.class);

	private static class PairFuture implements Future<Pair<byte[], byte[]>>
	{
	
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}
	
		@Override
		public boolean isCancelled() {
			return false;
		}
		volatile private byte[] a;
		volatile private byte[] b;
		@Override
		public boolean isDone() {
			return a!=null&&b!=null;
		}
	
		@Override
		public Pair<byte[], byte[]> get() throws InterruptedException,
				ExecutionException {
			synchronized (this) {
				if(a==null||b==null)
				{
					this.wait();
				}
			}
			return new Pair<byte[], byte[]>(a,b);
		}
	
		@Override
		public Pair<byte[], byte[]> get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			synchronized (this) {
				if(a==null||b==null)
				{
					unit.timedWait(this, timeout);
				}
				if(a==null||b==null)
				{
					throw new TimeoutException();
				}
			}
			return new Pair<byte[], byte[]>(a,b);
		}
	
		public void setA(final byte[] bytearray) {
			synchronized (this) {
				if (bytearray == null) {
					a = null;
				} else {
					a = Arrays.copyOf(bytearray, bytearray.length);
				}
				if(b!=null)
				{
					this.notifyAll();
				}
			}
		}
	
		public void setB(final byte[] bytearray) {
			synchronized (this) {
				if (bytearray == null) {
					b = null;
				} else {
					b = Arrays.copyOf(bytearray, bytearray.length);
				}
				if(a!=null)
				{
					this.notifyAll();
				}
			}
		}
		
	}
	private UtilProcess() {
		// disable constructor of utility class
	}
	
	/**
	 * Execute a single command and collect its stdout into a String.
	 * The method blocks the
	 * caller thread until the stdout of the program is closed (typically until the program terminates).
	 * <p>
	 * Use only for running short programs!
	 * 
	 * @param command
	 * @return standard output of the command after p.getInputStream() reached
	 *         EOF
	 * @throws IOException
	 */
	public static String execute(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		return execute(p);
	}

	/**
	 * Collects the stdout of given process into a String (using UTF-8 encoding).
	 * The method blocks the
	 * caller thread until the stdout of the program is closed (typically until the program terminates).
	 * <p>
	 * Use only for running short programs!
	 * 
	 * @param process
	 * @return standard output of the command after process.getInputStream() reached
	 *         EOF
	 * @throws IOException
	 */
	public static String execute(Process process) throws IOException {
		return UtilFile.loadAsString(process.getInputStream());
	}

	/**
	 * Pipes the standard error and output stream of the given process to the
	 * standard error and standard output of the caller Java application.
	 * <p>
	 * There are two threads started that collect the stream contents in memory,
	 * and writes them at once after 'p' has been terminated
	 * 
	 * @param p The process to stream
	 */
	public static void streamOutputsOfProcess(final Process p) {
		new Thread() {
			public void run() {
				try {
					String outStream = UtilFile
							.loadAsString(p.getInputStream());
					// writing on stdout is required here
					System.out.println(outStream);// NOSONAR
				} catch (Exception e) {
					LOG.error("Streaming program's output stream terminated", e);
				}
			};
		}.start();
		new Thread() {
			public void run() {
				try {
					String errStream = UtilFile
							.loadAsString(p.getErrorStream());
					// writing on stderr is required here
					System.err.println(errStream);// NOSONAR
				} catch (Exception e) {
					LOG.error("Streaming program's error stream terminated", e);
				}

			};
		}.start();
	}

	/**
	 * Pipes the given input stream into given target output stream. The method
	 * starts a new thread that continuously writes the values into output. The
	 * thread is terminated if iStream reaches EOF.
	 * 
	 * @param iStream
	 *            The input stream to pipe into given target output stream
	 * @param target
	 *            The target output stream. It is not closed after input is consumed.
	 */
	public static void streamErrorOfProcess(final InputStream iStream,
			final OutputStream target) {
		new Thread() {
			public void run() {
				try {
					ConnectStreams.doStream(iStream,target);
				} catch (IOException e) {
					LOG.error("Exception during streaming error stream", e);
				}
			};
		}.start();
	}
	/**
	 * Saves the output of given process as a {@link Future} object. Results will
	 * be ready after both stderr and stdout are closed by the process, so {@link Future#get()} will
	 * block until the given process is being run.
	 * 
	 * Reading of the streams is done on separate processes. The method call itself returns at once.
	 * 
	 * @param p
	 * @return {@link Future} value to get the full output of the process as a pair of byte arrays. A is stdout, B is stderr.
	 * @since 6.1
	 */
	public static Future<Pair<byte[], byte[]>> saveOutputsOfProcess(final Process p)
	{
		final PairFuture retfut=new PairFuture();
		new Thread(){
			public void run() {
				ByteArrayOutputStream ret=new ByteArrayOutputStream();
				try {
					ConnectStreams.doStream(p.getInputStream(), ret);
				} catch (Exception e) {
					LOG.error("Error streaming std out",e);
				}finally
				{
					retfut.setA(ret.toByteArray());
				}
			};
		}
		.start();
		new Thread(){public void run() {
			ByteArrayOutputStream ret=new ByteArrayOutputStream();
			try {
				ConnectStreams.doStream(p.getErrorStream(), ret);
			} catch (Exception e) {
				LOG.error("Error streaming std err",e);
			}finally
			{
				retfut.setB(ret.toByteArray());
			}
		};}
		.start();
		return retfut;
	}
	/**
	 * Stop the process (if it is still alive) and return the exit code after.
	 * 
	 * In some cases the process is still alive after the streams (stderr, stdout) are closed.
	 * In some cases the exitValue throws {@link IllegalThreadStateException} after the process is destroyed.
	 * 
	 * Both problems are solved by a timeout when necessary. When timeout is not necessary this method does not wait but
	 * returns immediately.
	 * 
	 * @param p the process to get the exit code from
	 * @param timeoutBefore timeout to wait until the process becomes not alive
	 * @param timeoutAfter timeout to wait until the exit value can be read
	 * @return
	 * @throws InterruptedException 
	 */
	public static int stopAndGetExitCode(Process p, long timeoutBefore, long timeoutAfter) throws InterruptedException
	{
		if (isAlive(p)){
			Thread.sleep(timeoutBefore);
			if(isAlive(p))
			{
				p.destroy();
			}
		}
		try
		{
			p.exitValue();
		}catch(IllegalThreadStateException itse)
		{
			p.destroy();
			// Tolerate some timeout for exiting process.
			Thread.sleep(timeoutAfter);
		}
		return p.exitValue();
	}
	
	/**
	 * Uses Java 1.7 methods on Process to check if the Process is still alive.
	 * 
	 * @param p the Process to check
	 * @return <code>true</code> if the Process is still alive
	 */
	public static boolean isAlive(Process p) {
		boolean isAlive = false;
		try {
			p.exitValue();
		} catch (IllegalThreadStateException ex) {
			isAlive = true;
		}
		return isAlive;
	}
	/**
	 * Query the process every 1 second and return value when the process is terminated.
	 * @param p
	 * @return
	 */
	public static SignalFutureWrapper<Integer> getProcessReturnValueFuture(final Process p)
	{
		final SignalFutureWrapper<Integer> ret=new SignalFutureWrapper<Integer>();
		Callable<Object> test=new Callable<Object>() {
			@Override
			public Object call() {
				if(isAlive(p))
				{
					UtilTimer.getInstance().executeTimeout(1000, this);
				}else
				{
					ret.ready(p.exitValue(), null);
				}
				return null;
			}
		};
		try {
			test.call();
		} catch (Exception e) {
			// Never happens
			//NOSONAR empty on purpose
		}
		return ret;
	}
}
