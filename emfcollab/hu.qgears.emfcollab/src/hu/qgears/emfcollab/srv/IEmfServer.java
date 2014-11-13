package hu.qgears.emfcollab.srv;

import hu.qgears.emfcollab.exceptions.EmfCommandTimeout;
import hu.qgears.emfcollab.exceptions.EmfExceptionConficting;
import hu.qgears.emfcollab.exceptions.EmfExceptionNotSaved;
import hu.qgears.emfcollab.exceptions.EmfModelAlreadyLocked;

import java.io.IOException;


/**
 * Interface of the server of a single resource set.
 * @author rizsi
 *
 */
public interface IEmfServer {
	/**
	 * The current (initial from the client's perspective)
	 * state of the EMF server. See documentation of return type.
	 * @return the current (initial from the client's perspective)
	 * 	state of the server.
	 */
	EmfInitialState initializeClient(EmfSessionKey sessionKey, IEmfClientCallback clientCallback);
	/**
	 * Lock the model for executing a new command defined by the client
	 * @return the command id
	 */
	long lockModelForCommand(EmfSessionKey sessionKey, long currentStateIndex) throws EmfModelAlreadyLocked;
	/**
	 * Execute a command on the server.
	 * The command was already executed on the client and the
	 * model must be locked so this method can not fail
	 * (in theory :-) so it only throws runtime exception.
	 * @throws EmfModelAlreadyLocked 
	 */
	void executeCommand(EmfSessionKey sessionKey, EmfCommand command) throws EmfCommandTimeout, EmfModelAlreadyLocked;
	/**
	 * Do undo on the model.
	 * @return the new state id of the server after that is undone
	 * @throws EmfModelAlreadyLocked in case that undo is impossible because
	 * some events are being processed now.
	 * @throws EmfExceptionConficting 
	 */
	long tryUndo(EmfSessionKey sessionKey, long currentStateIndex, long commandId) throws EmfModelAlreadyLocked, EmfExceptionConficting;
	/**
	 * Do redo on the model.
	 * @return the new state id of the server after that is redone
	 * @throws EmfModelAlreadyLocked in case that undo is impossible because
	 * some events are being processed now.
	 * @throws EmfExceptionConficting 
	 */
	long tryRedo(EmfSessionKey sessionKey, long currentStateIndex, long commandId) throws EmfModelAlreadyLocked, EmfExceptionConficting;
	/**
	 * Save model
	 * @param commitLog
	 * @throws IOException in case something happens when saving
	 */
	void saveModel(EmfSessionKey sessionKey, EmfCredentials credentials, String commitLog) throws IOException;
	/**
	 * Commit model.
	 * 
	 * Deletes undo and redo logs. 
	 * Commit model to version control. Only applies in case there is a version control system.
	 * 
	 * @param commitLog
	 * @throws IOException in case something happens when saving
	 */
	void commitModel(EmfSessionKey sessionKey, EmfCredentials credentials, String commitLog) throws IOException;
	/**
	 * Dispose server side model.
	 * 
	 * Use cases:
	 * * dispose the model so it is going to be updated from SVN when next loaded.
	 * * dispose the model because the model got corrupt.
	 * @param sessionKey
	 * @param force dispose even if it is not saved
	 * @throws EmfModelAlreadyLocked 
	 * @throws EmfExceptionNotSaved 
	 */
	void disposeServerSideModel(EmfSessionKey sessionKey, boolean force) throws EmfModelAlreadyLocked, EmfExceptionNotSaved;
	/**
	 * Revert server side model - return to the HEAD version of version control.
	 * @param sessionKey
	 * @throws EmfModelAlreadyLocked
	 */
	void revertServerSideModel(EmfSessionKey sessionKey) throws EmfModelAlreadyLocked;
	
	/**
	 * Disconnect this client from the server.
	 * @param sessionKey
	 */
	void disconnectClient(EmfSessionKey sessionKey);
}
