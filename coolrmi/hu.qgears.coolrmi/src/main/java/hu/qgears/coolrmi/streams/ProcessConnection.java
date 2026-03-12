package hu.qgears.coolrmi.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessConnection implements IConnection
{
	private Process process;
	private ConnectionConfiguration configuration=new ConnectionConfiguration();
	public ProcessConnection(Process process) {
		super();
		this.process = process;
//		process.onExit().thenAccept(p->{
//			try {
//				System.out.println("Process exited!");
//				p.getOutputStream().close();
//				p.getInputStream().close();
//				System.out.println("All closed!");
//			} catch (IOException e) {
//			}
//		});
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return process.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return process.getOutputStream();
	}

	@Override
	public void close() {
		try {
			process.destroy();
		} catch (Exception e) {
			configuration.getLog().logError(e);
		}
	}
	@Override
	public ConnectionConfiguration getConfiguration() {
		return configuration;
	}
}
