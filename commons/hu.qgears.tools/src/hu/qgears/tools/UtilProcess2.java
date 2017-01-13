package hu.qgears.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Helper methods for communicating with external processes.
 */
public class UtilProcess2 {
	public static class ProcessResult
	{
		private boolean outClosed;
		private boolean errClosed;
		private Throwable error;
		private Process p;
		private OutputStream out;
		private OutputStream err;
		public ProcessResult(Process p, OutputStream out, OutputStream err) {
			this.p=p;
			this.out=out;
			this.err=err;
		}

		public boolean isDone() {
			synchronized (this) {
				return (outClosed&&errClosed) || (error!=null);
			}
		}

		public void outputClosed() {
			synchronized (this) {
				outClosed=true;
				signalIfReady();
			}
		}

		private void signalIfReady() {
			if(isDone())
			{
				this.notifyAll();
			}
		}

		public void errorClosed() {
			synchronized (this) {
				errClosed=true;
				signalIfReady();
			}
		}

		public void setError(Exception e) {
			synchronized (this) {
				if(error==null)
				{
					error=e;
				}
				signalIfReady();
			}
		}

		public ProcessResult get() throws ExecutionException {
			if(error!=null)
			{
				throw new ExecutionException(error);
			}
			return this;
		}
		public byte[] getStdout()
		{
			return ((ByteArrayOutputStream)out).toByteArray();
		}
		public String getStdoutString()
		{
			return new String(getStdout(), StandardCharsets.UTF_8);
		}
		public String getStderrString()
		{
			return new String(getStderr(), StandardCharsets.UTF_8);
		}
		public byte[] getStderr()
		{
			return ((ByteArrayOutputStream)err).toByteArray();
		}
		public int getExitCode() throws InterruptedException
		{
			return UtilProcess2.stopAndGetExitCode(p, 50, 500);
		}
	}
	public static class ProcessFuture implements Future<ProcessResult>
	{
		private Process p;
		public ProcessFuture(final Process p, final OutputStream out, final OutputStream err) {
			this.p=p;
			result=new ProcessResult(p, out, err);
			new Thread("Process stream output"){
				@Override
				public void run() {
					try
					{
						doStream(p.getInputStream(), out);
					}catch (Exception e) {
						result.setError(e);
					}
					finally
					{
						result.outputClosed();
					}
				}
			}.start();
			new Thread("Process stream error"){
				@Override
				public void run() {
					try
					{
						doStream(p.getErrorStream(), err);
					}catch (Exception e) {
						result.setError(e);
					}
					finally
					{
						result.errorClosed();
					}
				}
			}.start();
		}
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}
	
		@Override
		public boolean isCancelled() {
			return false;
		}
		private ProcessResult result;
		@Override
		public boolean isDone() {
			return result.isDone();
		}
	
		@Override
		public ProcessResult get() throws InterruptedException,
				ExecutionException {
			synchronized (result) {
				while(!result.isDone())
				{
					result.wait();
				}
			}
			return result.get();
		}
	
		@Override
		public ProcessResult get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			synchronized (result) {
				if(!result.isDone())
				{
					unit.timedWait(result, timeout);
				}
				if(!result.isDone())
				{
					throw new TimeoutException();
				}
			}
			return result.get();
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
		public int stopAndGetExitCode(long timeoutBefore, long timeoutAfter) throws InterruptedException
		{
			return UtilProcess2.stopAndGetExitCode(p, timeoutBefore, timeoutAfter);
		}
	}
	private UtilProcess2() {
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
	public static ProcessFuture execute(String command) throws IOException {
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
	public static ProcessFuture execute(Process process) throws IOException {
		return new ProcessFuture(process, new ByteArrayOutputStream(), new ByteArrayOutputStream());
	}
	
	private static void doStream(final InputStream iStream,
			final OutputStream target) throws IOException {
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
}