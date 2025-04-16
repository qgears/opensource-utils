package hu.qgears.coolrmi.remoter;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.qgears.commons.NamedThreadFactory;
import hu.qgears.coolrmi.multiplexer.ISocketMultiplexerListener;
import hu.qgears.coolrmi.multiplexer.SocketMultiplexer;
import hu.qgears.coolrmi.streams.IConnection;

abstract public class CoolRMIRemoter extends GenericCoolRMIRemoter
{
	private class SocketMultiplexerListener implements ISocketMultiplexerListener
	{

		@Override
		public void messageReceived(byte[] msg) {
			CoolRMIRemoter.this.messageReceived(msg);
		}

		@Override
		public void pipeBroken(Exception e) {
			CoolRMIRemoter.this.pipeBroken(e);
		}
		
	}
	private IConnection sock;
	private Executor serverSideExecutor = null;

	public CoolRMIRemoter(ClassLoader classLoader, boolean guaranteeOrdering) {
		super(classLoader, guaranteeOrdering);
		if(guaranteeOrdering)
		{
			serverSideExecutor=Executors.newSingleThreadExecutor(new NamedThreadFactory("Cool RMI executor"));
		}else
		{
			serverSideExecutor=new Executor()
			{
				@Override
				public void execute(Runnable command) {
					Thread th=new Thread(command, "Cool RMI executor");
					th.start();
				}
			};
		}
	}
	protected void connect(IConnection sock) throws IOException {
		this.sock = sock;
		multiplexer = new SocketMultiplexer(
				sock.getInputStream(), sock
				.getOutputStream(), new SocketMultiplexerListener(), guaranteeOrdering, isClient());
		connected = true;
		((SocketMultiplexer)multiplexer).start();
	}
	abstract protected boolean isClient();
	@Override
	protected void closeConnection() throws IOException {
		sock.close();
		if(serverSideExecutor instanceof ExecutorService)
		{
			((ExecutorService) serverSideExecutor).shutdown();
		}
	}
	public IConnection getConnection()
	{
		return sock;
	}
	@Override
	public void execute(Runnable runnable) {
		serverSideExecutor.execute(runnable);
	}
}
