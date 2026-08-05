package hu.qgears.emfcollab.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import hu.qgears.commons.UtilTimer;
import hu.qgears.emfcollab.EmfCommandExecutor;
import hu.qgears.emfcollab.exceptions.EmfExceptionConficting;
import hu.qgears.emfcollab.exceptions.EmfExceptionNotSaved;
import hu.qgears.emfcollab.exceptions.EmfModelAlreadyLocked;
import hu.qgears.emfcollab.exceptions.EmfRuntimeException;
import hu.qgears.emfcollab.serial.Serializate;
import hu.qgears.emfcollab.srv.EmfCommand;
import hu.qgears.emfcollab.srv.EmfCredentials;
import hu.qgears.emfcollab.srv.EmfInitialState;
import hu.qgears.emfcollab.srv.EmfSessionKey;
import hu.qgears.emfcollab.srv.IEmfClientCallback;
import hu.qgears.emfcollab.srv.IEmfServer;
import hu.qgears.emfcollab.util.UtilEmf;
import hu.qgears.emfcollab.util.UtilEmfModelIO;

public class EmfServer implements IEmfServer {
	private String resoruceName;
	private EmfMultiServer parent;
	private long commandTimeout=10000;
	private byte[] initialModel;
	private long stateIndex=0;
	private long savedStateIndex=0;
	private long lockCount;
	private long lockCountCtr=0;
	private boolean locked=false;
	private List<EmfClientWrapper> callbacks=new ArrayList<EmfClientWrapper>();
	private LoadedResource loadedResource;
	private IEmfServerListener listener;
	private List<EmfCommand> undoCommandStack=new ArrayList<EmfCommand>();
	private List<EmfCommand> redoCommandStack=new ArrayList<EmfCommand>();
	private EmfCommandExecutor commandExecutor=new EmfCommandExecutor();
	public static final long defaultTimeoutMillis=60000;
	Serializate log;

	public EmfServer(EmfMultiServer parent, String resoruceName, File logFile) throws FileNotFoundException, UnsupportedEncodingException {
		super();
		this.parent = parent;
		this.resoruceName=resoruceName;
		if(logFile!=null)
		{
			FileOutputStream fos=new FileOutputStream(logFile, true);
			OutputStreamWriter logOsw=new OutputStreamWriter(fos, "UTF-8");
			log=new Serializate(logOsw);
		}
	}
//	private long clientIdCounter;
	public synchronized void init(ResourceWithHistory loadedResource, IEmfServerListener listener) throws IOException
	{
		if(log!=null)
		{
			log.log("INIT");
		}
		this.loadedResource=loadedResource.getResource();
		this.undoCommandStack=new ArrayList<EmfCommand>(loadedResource.getUndoList());
		this.redoCommandStack=new ArrayList<EmfCommand>(loadedResource.getRedoList());
		this.listener=listener;
		initialModel=UtilEmfModelIO.saveModelToMemory(loadedResource.getResource());
		commandExecutor.init(loadedResource.getResource());
		for(EmfCommand c: undoCommandStack)
		{
			try
			{
				commandExecutor.executeEvents(c.getEvents());
			}catch(Exception e)
			{
				logInternalError("Exceuting model commands when loading model", e);
			}
		}
	}
	private void logInternalError(String string, Exception e) {
		System.err.println("Internal recoverable error: "+string);
		e.printStackTrace();
	}
	private void checkClientsAlive()
	{
		synchronized (callbacks) {
			Iterator<EmfClientWrapper> it=callbacks.iterator();
			while(it.hasNext())
			{
				EmfClientWrapper w=it.next();
				if(!w.isAlive())
				{
//					System.out.println("Client removed!");
					it.remove();
				}
			}
		}
	}
	@Override
	public synchronized void executeCommand(
			EmfSessionKey sessionKey,
			final EmfCommand command) throws EmfModelAlreadyLocked {
		EmfSession session=parent.checkSessionKey(sessionKey);
		if(!(command.getCommandIndex()==stateIndex+1))
		{
			throw new EmfModelAlreadyLocked();
		}
		commandExecutor.executeEvents(command.getEvents());
		logExec(command);
		undoCommandStack.add(command);
		locked=false;
		stateIndex=command.getCommandIndex();
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.commandExecuted(session.getSessionId(), stateIndex, command);
				}catch(Exception e)
				{
					logCallbackException(callback, e);
				}
			}
		}
	}
	@Override
	public synchronized EmfInitialState initializeClient(
			EmfSessionKey sessionKey,
			IEmfClientCallback clientCallback) {
		EmfSession session=parent.checkSessionKey(sessionKey);
		if(log!=null)
		{
			log.log(session, "INITCLIENT");
		}
		EmfInitialState ret=new EmfInitialState();
		ret.setXmiFile(initialModel);
		ret.setCurrentUndoStack(undoCommandStack);
		ret.setCurrentRedoStack(redoCommandStack);
		ret.setStateIndex(stateIndex);
		ret.setSavedStateIndex(savedStateIndex);
		synchronized (callbacks) {
			callbacks.add(new EmfClientWrapper(this, sessionKey, clientCallback));
		}
		return ret;
	}
	class CheckModelLockTimeOut implements Callable<Object>
	{
		long lockCount;

		public CheckModelLockTimeOut(long lockCount) {
			super();
			this.lockCount = lockCount;
		}

		@Override
		public Object call() throws Exception {
			checkTimeout(lockCount);
			return null;
		}
		
	}
	@Override
	public synchronized long lockModelForCommand(
			EmfSessionKey sessionKey,
			long currentStateIndex) throws EmfModelAlreadyLocked {
		@SuppressWarnings("unused")
		EmfSession session=parent.checkSessionKey(sessionKey);
		checkLocked(currentStateIndex);
		locked=true;
		lockCount=lockCountCtr++;
		UtilTimer.getInstance().executeTimeout(commandTimeout, new CheckModelLockTimeOut(lockCount));
		return stateIndex+1;
	}
	public synchronized void checkTimeout(long lockCount) {
		if(lockCount==EmfServer.this.lockCount)
		{
			locked=false;
		}
	}
	@Override
	public synchronized long tryUndo(
			EmfSessionKey sessionKey,
			long currentStateIndex, long commandId) throws EmfModelAlreadyLocked, EmfExceptionConficting {
		EmfSession session=parent.checkSessionKey(sessionKey);
		checkLocked(currentStateIndex);
		EmfCommand command=getUndoCommand(commandId);
		// TODO check whether undoing the command is still valid or not
		// Do undo
		commandExecutor.undoEvents(command.getEvents());
		logUndo(command);
		undoCommandStack.remove(command);
		redoCommandStack.add(command);
		stateIndex++;
		long newStateIndex=stateIndex;
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.commandUndone(newStateIndex, session.getSessionId(), command.getCommandIndex());
				}catch (Exception e) {
					logCallbackException(callback, e);
				}
			}
		}
		return newStateIndex;
	}
	private EmfCommand getUndoCommand(long commandId) {
		for(int i=undoCommandStack.size()-1;i>=0;--i)
		{
			EmfCommand c=undoCommandStack.get(i);
			if(c.getCommandIndex()==commandId)
			{
				return c;
			}
		}
		throw new EmfRuntimeException("Undo command not found in undo stack");
	}
	@Override
	public long tryRedo(EmfSessionKey sessionKey, 
			long currentStateIndex, long commandId) throws EmfModelAlreadyLocked, EmfExceptionConficting {
		EmfSession session=parent.checkSessionKey(sessionKey);
		checkLocked(currentStateIndex);
		EmfCommand command=getRedoCommand(commandId);
		commandExecutor.executeEvents(command.getEvents());
		logRedo(command);
		redoCommandStack.remove(command);
		undoCommandStack.add(command);
		stateIndex++;
		long newStateIndex=stateIndex;
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.commandRedone(newStateIndex, session.getSessionId(), command.getCommandIndex());
				}catch(Exception e)
				{
					logCallbackException(callback, e);
				}
			}
		}
		return newStateIndex;
	}
	private void logCallbackException(EmfClientWrapper callback, Exception e) {
		System.err.println("Error executing on client: "+ callback);
		e.printStackTrace();
	}
	private EmfCommand getRedoCommand(long commandId) {
		for(int i=redoCommandStack.size()-1;i>=0;--i)
		{
			EmfCommand c=redoCommandStack.get(i);
			if(c.getCommandIndex()==commandId)
			{
				return c;
			}
		}
		throw new EmfRuntimeException("Redo command not found in redo stack");
	}
	/**
	 * Check whether server is locked for executing a command.
	 * 
	 * also check client state is in sync with server:
	 * in case of redo done on the server (initiated by an other client)
	 * and the requesting client did not execute it yet
	 * then the server must not allow the client to execute any commands!
	 * @param currentStateIndex
	 * @throws EmfModelAlreadyLocked
	 */
	private void checkLocked(long currentStateIndex) throws EmfModelAlreadyLocked {
		if(locked||currentStateIndex!=stateIndex)
		{
			throw new EmfModelAlreadyLocked();
		}
	}
	private void logExec(EmfCommand ev) {
		logserializate("EXECUTE", ev);
	}
	private void logUndo(EmfCommand ev) {
		logserializate("UNDO   ", ev);
	}
	private void logRedo(EmfCommand ev) {
		logserializate("REDO   ", ev);
	}
	private void logserializate(String string, EmfCommand ev) {
		if(log!=null)
		{
			try {
				log.serializate(string, ev);
				log.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public synchronized void saveModel(
			EmfSessionKey sessionKey,
			EmfCredentials credentials,
			String commitLog) throws IOException {
		EmfSession session=parent.checkSessionKey(sessionKey);
		if(log!=null)
		{
			log.log(session, "SAVE");
		}
		UtilEmf.sanitizeModel(getResourceWithHistory().getResource().getResource());
		listener.save(getResourceWithHistory(), credentials, commitLog);
		savedStateIndex=stateIndex;
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.modelSaved(session.getSessionId(), commitLog, stateIndex);
				}catch(Exception e)
				{
					logCallbackException(callback, e);
				}
			}
		}
	}
	private ResourceWithHistory getResourceWithHistory() {
		ResourceWithHistory ret=new ResourceWithHistory(loadedResource,
				undoCommandStack, redoCommandStack);
		return ret;
	}
	@Override
	public synchronized void disposeServerSideModel(EmfSessionKey sessionKey, boolean force) throws EmfModelAlreadyLocked, EmfExceptionNotSaved {
		EmfSession session=parent.checkSessionKey(sessionKey);
		if(log!=null)
		{
			log.log(session, "DISPOSE");
		}
		if(locked)
		{
			throw new EmfModelAlreadyLocked();
		}
		if(isDirty()&&!force)
		{
			throw new EmfExceptionNotSaved();
		}
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.modelDisposed(session.getSessionId());
				}catch (Exception e) {
					logCallbackException(callback, e);
				}
			}
		}
		dispose();
	}
	private boolean isDirty()
	{
		return stateIndex!=savedStateIndex;
	}
	private void dispose()
	{
		parent.serverDisposed(this);
		checkClientsAlive();
		for(EmfClientWrapper cli: callbacks)
		{
			try
			{
				cli.stopClient();
			}catch (Exception e) {
				logCallbackException(cli, e);
			}
		}
		log.dispose();
	}
	public String getResoruceName() {
		return resoruceName;
	}
	@Override
	public synchronized void commitModel(EmfSessionKey sessionKey,
			EmfCredentials credentials, String commitLog) throws IOException {
		EmfSession session=parent.checkSessionKey(sessionKey);
		if(log!=null)
		{
			log.log(session, "COMMIT");
		}
		UtilEmf.sanitizeModel(getResourceWithHistory().getResource().getResource());
		listener.commit(getResourceWithHistory(), credentials, commitLog);
		initialModel=UtilEmfModelIO.saveModelToMemory(loadedResource);
		redoCommandStack.clear();
		undoCommandStack.clear();
		savedStateIndex=stateIndex;
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.modelCommitted(session.getSessionId(), commitLog, stateIndex);
				}catch (Exception e) {
					logCallbackException(callback, e);
				}
			}
		}
	}
	@Override
	public void revertServerSideModel(EmfSessionKey sessionKey)
			throws EmfModelAlreadyLocked {
		EmfSession session=parent.checkSessionKey(sessionKey);
		if(log!=null)
		{
			log.log(session, "REVERT");
		}
		if(locked)
		{
			throw new EmfModelAlreadyLocked();
		}
		checkClientsAlive();
		synchronized (callbacks) {
			for(EmfClientWrapper callback:callbacks)
			{
				try
				{
					callback.modelDisposed(session.getSessionId());
				}catch (Exception e) {
					logCallbackException(callback, e);
				}
			}
		}
		listener.revert(loadedResource);
		dispose();
	}
	@Override
	public synchronized void disconnectClient(EmfSessionKey sessionKey) {
		parent.checkSessionKey(sessionKey);
		synchronized (callbacks) {
			for(EmfClientWrapper c: callbacks)
			{
				if(c.getSessionKey().getClientId().getId()==sessionKey.getClientId().getId())
				{
					c.stopClient();
				}
			}
		}
		checkClientsAlive();
	}
}
