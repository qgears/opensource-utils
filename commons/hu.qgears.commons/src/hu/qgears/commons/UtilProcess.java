package hu.qgears.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

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
				while(a==null||b==null)
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
					this.wait();
				}
				if(a==null||b==null)
				{
					throw new TimeoutException();
				}
			}
			return new Pair<byte[], byte[]>(a,b);
		}
	
		public void setA(byte[] string) {
			synchronized (this) {
				a=string;
				this.notifyAll();
			}
		}
	
		public void setB(byte[] string) {
			synchronized (this) {
				b=string;
				this.notifyAll();
			}
		}
		
	}
	private UtilProcess() {
		// disable constructor of utility class
	}
	
	/**
	 * Execute a single command and collect its stdout into a String. The method
	 * blocks the caller thread until the given program terminates.
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
	 * Collects the stdout of given process into a String. The method blocks the
	 * caller thread until the given program terminates.
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
	 * Pipes the given input stream into given target ouput stream. The method
	 * starts a new thread that continuously writes the values into output. The
	 * thread is terminated if iStream reaches EOF.
	 * 
	 * @param iStream
	 *            The input stream to pipe into given target output stream
	 * @param target
	 *            The target output stream
	 */
	public static void streamErrorOfProcess(final InputStream iStream,
			final OutputStream target) {
		new Thread() {
			public void run() {
				try {
					doStream();
				} catch (IOException e) {
					LOG.error("Exception during streaming error stream", e);
				}
			};
			private void doStream() throws IOException {
				try {
					int n;
					byte[] cbuf = new byte[1024];
					while ((n = iStream.read(cbuf)) > -1) {
						target.write(cbuf, 0, n);
						target.flush();
					}
				} finally {
					if (iStream != null) {
						iStream.close();
					}
				}
			}
		}.start();
	}
	
	/**
	 * Saves the ouput of given process as a {@link Future} object. Results will
	 * be ready if the given process terminates, so {@link Future#get()} will
	 * block until the given process is run.
	 * 
	 * @param p
	 * @return
	 * @since 6.1
	 */
	public static Future<Pair<byte[], byte[]>> saveOutputsOfProcess(final Process p)
	{
		final PairFuture retfut=new PairFuture();
		new Thread(){
			public void run() {
				ByteArrayOutputStream ret=new ByteArrayOutputStream();
				try {
					UtilProcess.streamErrorOfProcess(p.getInputStream(), ret);
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
				UtilProcess.streamErrorOfProcess(p.getErrorStream(), ret);
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
}
