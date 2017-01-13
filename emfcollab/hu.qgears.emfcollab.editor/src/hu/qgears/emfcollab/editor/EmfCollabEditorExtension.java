package hu.qgears.emfcollab.editor;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.ICoolRMIProxy;
import hu.qgears.coolrmi.UtilEvent;
import hu.qgears.emfcollab.EmfCommandExecutor;
import hu.qgears.emfcollab.EmfEvent;
import hu.qgears.emfcollab.EmfSynchronizatorListener;
import hu.qgears.emfcollab.XmiIdSource;
import hu.qgears.emfcollab.editor.internal.Activator;
import hu.qgears.emfcollab.exceptions.EmfExceptionConficting;
import hu.qgears.emfcollab.exceptions.EmfExceptionNotSaved;
import hu.qgears.emfcollab.exceptions.EmfModelAlreadyLocked;
import hu.qgears.emfcollab.impl.EmfServer;
import hu.qgears.emfcollab.impl.LoadedResource;
import hu.qgears.emfcollab.srv.EmfCommand;
import hu.qgears.emfcollab.srv.EmfCredentials;
import hu.qgears.emfcollab.srv.EmfInitialState;
import hu.qgears.emfcollab.srv.EmfSessionId;
import hu.qgears.emfcollab.srv.EmfSessionKey;
import hu.qgears.emfcollab.srv.IEmfClientCallback;
import hu.qgears.emfcollab.srv.IEmfMultiServer;
import hu.qgears.emfcollab.srv.IEmfServer;
import hu.qgears.emfcollab.util.UtilEmfModelIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;


public class EmfCollabEditorExtension {
	private UtilEvent<EmfCollabEditorExtension> diposeEvent=new UtilEvent<EmfCollabEditorExtension>();
	private IDisposeListener disposeListener;
	private EmfSessionKey sessionKey;
	private EmfCredentials credentials;
	private EmfCollabCommandStack commandStack;
	private Display displayThread;
	private ClassLoader classLoader;
	volatile private boolean disposed=false;
	private boolean remoteExecuting=false;
	private long commandIndex;
	private EmfSynchronizatorListener sync = new EmfSynchronizatorListener();
	private IEmfServer server;
	private XmiIdSource idSource;
	private EmfCommandExecutor emfCommandExecutor;
	private ICommandExecutor executor=new SimpleCommandExecutor();
	IRemoteCommandExecutionListener remoteExecutionListener=new NullRemoteCommandxecutionListener();
	public IRemoteCommandExecutionListener getRemoteExecutionListener() {
		return remoteExecutionListener;
	}
	public void setRemoteExecutionListener(
			IRemoteCommandExecutionListener remoteExecutionListener) {
		this.remoteExecutionListener = remoteExecutionListener;
	}
	public void setExecutor(ICommandExecutor executor) {
		this.executor = executor;
	}
	private EmfSessionId clientId;
	private XMIResource res;
	private long currentState;
	private long savedState;

	public class EmfClientCallback implements IEmfClientCallback
	{

		@Override
		public void commandExecuted(EmfSessionId sessionId, final long newStateIndex, final EmfCommand command) {
			if(disposed)
			{
				return;
			}
			serverCommandExecuted(sessionId, newStateIndex, command);
		}

		@Override
		public void commandRedone(EmfSessionId sessionId, final long newStateIndex, long commandId) {
			if(disposed)
			{
				return;
			}
			serverCommandRedone(newStateIndex, sessionId, commandId);
		}

		@Override
		public void commandUndone(EmfSessionId sessionId, final long newStateIndex, long commandId) {
			if(disposed)
			{
				return;
			}
			serverCommandUndone(newStateIndex, sessionId, commandId);
		}

		@Override
		public void modelSaved(EmfSessionId client, String message,
				long savedAtState) {
			if(disposed)
			{
				return;
			}
			serverModelSaved(savedAtState);
		}

		@Override
		public void modelDisposed(EmfSessionId sessionId) {
			if(disposed)
			{
				return;
			}
			serverModelDisposed();
		}

		@Override
		public void modelCommitted(EmfSessionId client, String message,
				long savedAtState) {
			if(disposed)
			{
				return;
			}
			serverModelCommitted(savedAtState);
		}
		
	}
	public ICommandExecutor getExecutor() {
		return executor;
	}
	public void serverModelDisposed() {
		displayThread.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				disposeListener.serverModelDisposed();
			}
		});
	}
	protected void serverExecuteCommand(final long newStateIndex, EmfCommand command) {
		remoteExecuting = true;
		try {
			remoteExecutionListener.remoteExecutionStart();
			EmfCommandWrapper comm=new EmfCommandWrapper(getEmfCommandExecutor(), command);
//			ExecuteRemoteCommand comm = new ExecuteRemoteCommand(this, command.getEvents());
			commandStack.execute(comm);
			if(newStateIndex!=-1)
			{
				setCurrentState(newStateIndex);
			}
		} finally {
			remoteExecuting = false;
			remoteExecutionListener.remoteExecutionEnd();
		}
	}
	public void serverModelSaved(long savedAtState) {
		savedState=savedAtState;
		displayThread.asyncExec(new Runnable() {
			@Override
			public void run() {
				commandStack.notifyListeners();
			}
		});
	}
	public void serverModelCommitted(long savedAtState) {
		savedState=savedAtState;
		displayThread.asyncExec(new Runnable() {
			@Override
			public void run() {
				commandStack.clearBuffers();
				commandStack.notifyListeners();
			}
		});
	}
	public boolean isDirty()
	{
		return savedState!=currentState;
	}
	private void setCurrentState(long newStateIndex) {
		if(currentState+1!=newStateIndex)
		{
			// Check state index
			// While client is in sync with server this state works as a counter!
			dispose();
			throw new RuntimeException("EMF collab client sync error: "+currentState+" to "+newStateIndex);
		}
		currentState=newStateIndex;
	}
	protected void serverUndoCommand(final long newStateIndex, long commandId) {
		remoteExecuting = true;
		try {
			remoteExecutionListener.remoteExecutionStart();
			commandStack.undoCallback(commandId);
			setCurrentState(newStateIndex);
		} finally {
			remoteExecuting = false;
			remoteExecutionListener.remoteExecutionEnd();
		}
	}
	public void serverCommandUndone(final long newStateIndex, EmfSessionId clientId, final long commandId) {
		displayThread.asyncExec(new Runnable() {
			@Override
			public void run() {
				serverUndoCommand(newStateIndex, commandId);
			}
		});
	}
	public void serverCommandRedone(final long newStateIndex, EmfSessionId clientId, final long commandId) {
		displayThread.asyncExec(new Runnable() {
			@Override
			public void run() {
				serverRedoCommand(newStateIndex, commandId);
			}
		});
	}
	private void serverRedoCommand(final long newStateIndex, long commandId) {
		remoteExecuting = true;
		try {
			remoteExecutionListener.remoteExecutionStart();
			commandStack.redoCallback(commandId);
			setCurrentState(newStateIndex);
		} finally {
			remoteExecuting = false;
			remoteExecutionListener.remoteExecutionEnd();
		}
	}
	public void serverCommandExecuted(EmfSessionId clientId, final long newStateIndex, final EmfCommand command) {
		if (!clientId.sameClient(this.clientId)) {
			displayThread.asyncExec(new Runnable() {
				@Override
				public void run() {
					serverExecuteCommand(newStateIndex, command);
				}
			});
		}
	}
	public EmfCollabEditorExtension(ClassLoader classLoader, IDisposeListener disposeListener) {
		super();
		this.classLoader = classLoader;
		this.disposeListener=disposeListener;
	}

	public Resource loadModel(ResourceSet resourceSet,
			IFileEditorInput f,
			EmfCredentials credentials) throws Exception {
		return loadModel(resourceSet, f.getFile(), credentials);
	
	}
	public Resource loadModel(ResourceSet resourceSet,
			IFile f,
			EmfCredentials credentials) throws Exception {
		this.credentials=credentials;
		idSource = new XmiIdSource();
		String address = UtilFile.loadAsString(f
				.getContents());
		List<String> pieces = UtilString.split(address, "\r\n");
		String host = pieces.get(0);
		int port = Integer.parseInt(pieces.get(1));
		String resourceName = pieces.get(2);
		SocketAddress socketAddress = new InetSocketAddress(host, port);
		CoolRMIClient client = new CoolRMIClient(classLoader, socketAddress,
				true);
		client.setTimeoutMillis(EmfServer.defaultTimeoutMillis);
		IEmfMultiServer multiServer = (IEmfMultiServer) client.getService(
				IEmfMultiServer.class, IEmfMultiServer.class.getName());
		try {
			sessionKey=multiServer.login(credentials);
			clientId=sessionKey.getClientId();
			server = multiServer.getServerForResource(sessionKey, credentials, resourceName);
			emfCommandExecutor = new EmfCommandExecutor();
			client.getServiceRegistry().addProxyType(EmfClientCallback.class,
					IEmfClientCallback.class);
			IEmfClientCallback callback = new EmfClientCallback();
			EmfInitialState initialState = server.initializeClient(sessionKey,
					callback);
			res = UtilEmfModelIO.loadFile(f
					.getFullPath().toString(), initialState.getXmiFile(),
					resourceSet);
			emfCommandExecutor.init(new LoadedResource(res, idSource));
			sync.init(res, idSource);
			List<EmfCommand> commands=initialState.getCurrentUndoStack();
			for(EmfCommand command:commands)
			{
				serverExecuteCommand(-1, command);
			}
			currentState=initialState.getStateIndex();
			savedState=initialState.getSavedStateIndex();
			commandStack.setInitialRedoCommands(initialState.getCurrentRedoStack());
			return res;
		} finally {
			((ICoolRMIProxy) multiServer).disposeProxy();
		}
	}
	
	/**
	 * Create the command stack. Either this method or setCommandStack must be called once on
	 * initialization.
	 * @return
	 */
	public EmfCollabCommandStack createCommandStack() {
		commandStack=new EmfCollabCommandStack(this);
		return commandStack;
	}
	
	/**
	 * Set the command stack to be used. Either this method or createCommandStack must be called once on
	 * initialization.
	 * 
	 * @param commandStack
	 */
	public void setCommandStack(EmfCollabCommandStack commandStack)
	{
		this.commandStack=commandStack;
	}
	public UtilEvent<EmfCollabEditorExtension> getDiposeEvent() {
		return diposeEvent;
	}
	public void dispose()
	{
		disposed=true;
		server.disconnectClient(sessionKey);
		((ICoolRMIProxy)server).disposeProxy();
		diposeEvent.eventHappened(this);
	}
	public EmfCommandExecutor getEmfCommandExecutor() {
		return emfCommandExecutor;
	}
	static int retryLatency=1000; 
	public void setDisplayThread(Display displayThread) {
		this.displayThread = displayThread;
		installRetryTimer(displayThread);
	}
	private List<Command> scheduledCommands=new ArrayList<Command>();
	public void scheduleForLaterExecution(Command command) {
		scheduledCommands.add(command);
	}

	private void installRetryTimer(final Display displayThread) {
		displayThread.timerExec(retryLatency, new Runnable() {
			@Override
			public void run() {
				try
				{
					if(scheduledCommands.size()>0)
					{
						Command command=scheduledCommands.remove(0);
						commandStack.execute(command);
					}
				}finally
				{
					if(!disposed)
					{
						displayThread.timerExec(retryLatency, this);
					}
				}
			}
		});
		
	}
	/**
	 * Is it possible to run a command?
	 * 
	 * Two cases are possible:
	 *  * A command from server side is being executed. In this case we
	 *    must allow the command to be done
	 *  *  
	 * @return whether execution of the command is allowed or not.
	 */
	public boolean commandAboutToExecute() {
		if (remoteExecuting) {
			return true;
		}
		try {
			commandIndex = server.lockModelForCommand(sessionKey, currentState);
			sync.getAndClearEventsCollected();
		} catch (EmfModelAlreadyLocked e) {
			return false;
		}
		return true;
	}

	public EmfCommand commandExecuted(String label) {
		if(!remoteExecuting)
		{
			try {
			List<EmfEvent> events = sync.getAndClearEventsCollected();
			EmfCommand command = new EmfCommand(commandIndex,
					""+label, events, getClientId());
				server.executeCommand(sessionKey, command);
			setCurrentState(commandIndex);
			return command;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	public void commandTryUndo(EmfCommandWrapper c) {
		try {
			server.tryUndo(sessionKey, currentState, c.getCommand().getCommandIndex());
//			setCurrentState(newStateIndex);
		} catch (EmfModelAlreadyLocked e) {
			Activator.getDefault().logError("Executing undo", e);
		} catch (EmfExceptionConficting e) {
			Activator.getDefault().logError("Executing undo", e);
		}
	}
	public void commandUndone() {
		// We dont need to do anything.
	}
	public void commandRedone() {
		// We dont need to do anything.
	}
	public void commandTryRedo(EmfCommandWrapper c)
	{
		try {
			server.tryRedo(sessionKey, currentState, c.getCommand().getCommandIndex());
		} catch (EmfModelAlreadyLocked e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmfExceptionConficting e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void doSave() throws IOException {
		server.saveModel(sessionKey, credentials, "Regular save");
	}
	public void disposeServrSideModel(boolean force) throws EmfModelAlreadyLocked, EmfExceptionNotSaved {
		server.disposeServerSideModel(sessionKey, force);
	}
	public EmfSessionId getClientId() {
		return clientId;
	}
	public EmfCollabCommandStack getCommandStack() {
		return commandStack;
	}
	public void commit(String log) {
		try {
			server.commitModel(sessionKey, credentials, log);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void revertServerSideModel() throws EmfModelAlreadyLocked {
		server.revertServerSideModel(sessionKey);
	}
}
