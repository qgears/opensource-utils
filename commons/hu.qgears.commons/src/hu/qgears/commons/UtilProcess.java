package hu.qgears.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class UtilProcess {

	private static Logger LOG = Logger.getLogger(UtilProcess.class);
	
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
}
