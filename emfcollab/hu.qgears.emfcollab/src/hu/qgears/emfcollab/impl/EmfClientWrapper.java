package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.srv.EmfCommand;
import hu.qgears.emfcollab.srv.EmfSessionId;
import hu.qgears.emfcollab.srv.EmfSessionKey;
import hu.qgears.emfcollab.srv.IEmfClientCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;


public class EmfClientWrapper {
	EmfServer server;
	volatile private boolean alive=true;
	private EmfSessionKey sessionKey;
	public boolean isAlive() {
		return alive;
	}
	IEmfClientCallback callback;
	public EmfClientWrapper(EmfServer server, EmfSessionKey sessionKey, IEmfClientCallback callback) {
		super();
		this.server=server;
		this.sessionKey=sessionKey;
		this.callback = callback;
		executor=Executors.newSingleThreadExecutor(new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "EMF Client");
			}
		});
	}
	ExecutorService executor;
	public void commandExecuted(final EmfSessionId sessionId, final long newStateIndex, final EmfCommand command) {
		exec(new Runnable() {
			@Override
			public void run() {
				try {
					callback.commandExecuted(sessionId, newStateIndex, command);
				} catch (Exception e) {
					stopClientOnError(e);
				}
			}
		});
	}
	private void exec(Runnable runnable) {
		try
		{
			executor.execute(runnable);
		}catch(RejectedExecutionException e)
		{
			alive=false;
			throw e;
		}
	}
	public void commandUndone(final long newStateIndex, final EmfSessionId clientId, final long commandId) {
		exec(new Runnable() {
			@Override
			public void run() {
				try {
					callback.commandUndone(clientId, newStateIndex, commandId);
				} catch (Exception e) {
					stopClientOnError(e);
				}
			}
		});
	}
	protected void stopClientOnError(Throwable t) {
		alive=false;
		System.err.println("Error executing EMFCollab server originated command on client. Client stopped");
		t.printStackTrace();
		executor.shutdown();
	}
	protected void stopClient() {
		alive=false;
		executor.shutdown();
		// TODO make sure that CoolRMI remoting is also disposed
	}
	public void modelSaved(final EmfSessionId client, final String message, final long savedAtState)
	{
		exec(new Runnable() {
			@Override
			public void run() {
				try {
					callback.modelSaved(client, message, savedAtState);
				} catch (Exception e) {
					stopClientOnError(e);
				}
			}
		});
	}

	public void commandRedone(final long newStateIndex, final EmfSessionId clientId, final long commandId) {
		exec(new Runnable() {
			@Override
			public void run() {
				try {
					callback.commandRedone(clientId, newStateIndex, commandId);
				} catch (Exception e) {
					stopClientOnError(e);
				}
			}
		});
	}
	public void modelDisposed(final EmfSessionId sessionId) {
		exec(new Runnable() {
			@Override
			public void run() {
				try {
					callback.modelDisposed(sessionId);
				} catch (Exception e) {
					stopClientOnError(e);
				}
			}
		});
	}
	public void modelCommitted(final EmfSessionId client, final String message, final long savedAtState)
	{
		exec(new Runnable() {
			@Override
			public void run() {
				try {
					callback.modelCommitted(client, message, savedAtState);
				} catch (Exception e) {
					stopClientOnError(e);
				}
			}
		});
	}
	public EmfSessionKey getSessionKey() {
		return sessionKey;
	}
}
