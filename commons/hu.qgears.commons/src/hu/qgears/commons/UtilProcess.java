package hu.qgears.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.concurrent.Future;

public class UtilProcess {
	/**
	 * Execute a single command
	 * and collect its stdout into a String.
	 * @param command
	 * @return standard output of the command after p.getInputStream() reached EOF
	 * @throws IOException
	 */
	public static String execute(String command) throws IOException
	{
		Process p=Runtime.getRuntime().exec(command);
		Reader r=new InputStreamReader(p.getInputStream(), "UTF-8");
		int n;
		char[] cbuf=new char[1024];
		StringBuilder ret=new StringBuilder();
		while((n=r.read(cbuf))>-1)
		{
			ret.append(cbuf, 0, n);
		}
		return ret.toString();
	}
	/**
	 * Wait for process to finish its task.
	 * @param p
	 * @return
	 * @throws IOException
	 */
	public static String execute(Process p) throws IOException
	{
		Reader r=new InputStreamReader(p.getInputStream(), "UTF-8");
		int n;
		char[] cbuf=new char[1024];
		StringBuilder ret=new StringBuilder();
		while((n=r.read(cbuf))>-1)
		{
			ret.append(cbuf, 0, n);
		}
		return ret.toString();
	}
	public static Future<Pair<String, String>> streamOutputsOfProcess(final Process p)
	{
		new Thread(){
			public void run() {
				StringBuilder ret=new StringBuilder();
				try {
					Reader r=new InputStreamReader(p.getInputStream(), "UTF-8");
					int n;
					char[] cbuf=new char[1024];
					while((n=r.read(cbuf))>-1)
					{
						ret.append(cbuf, 0, n);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(""+ret);
			};
		}
		.start();
		new Thread(){public void run() {
			StringBuilder ret=new StringBuilder();
			try {
				Reader r=new InputStreamReader(p.getErrorStream(), "UTF-8");
				int n;
				char[] cbuf=new char[1024];
				while((n=r.read(cbuf))>-1)
				{
					ret.append(cbuf, 0, n);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.err.println(""+ret);
		};}
		.start();
		return null;
	}
	public static void streamErrorOfProcess(final InputStream r, final OutputStream w) {
		new Thread(){public void run() {
			try {
				int n;
				byte[] cbuf=new byte[1024];
				while((n=r.read(cbuf))>-1)
				{
					w.write(cbuf, 0, n);
					w.flush();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};}
		.start();
	}
}
