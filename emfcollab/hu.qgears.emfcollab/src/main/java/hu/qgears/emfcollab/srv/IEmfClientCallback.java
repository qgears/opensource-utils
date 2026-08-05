package hu.qgears.emfcollab.srv;

/**
 * Interface the is offered by the EMF
 * client to the EMF server.
 * Changes initiated by other clients are sent through this interface.
 * @author rizsi
 *
 */
public interface IEmfClientCallback {
	/**
	 * Command was executed by a client.
	 * Commands executed by the listener client are also notified.
	 * Command ids are given in a single order so clients
	 * can find out when a command to be executed is not in order.
	 * 
	 * @param command
	 */
	void commandExecuted(EmfSessionId clientId, final long newStateIndex, EmfCommand command);
	/**
	 * Command was undone.
	 * The topmost command in the undo stack must be undone.
	 * The command Id is sent so client can check that it is still in sync with server.
	 * @param commandId
	 */
	void commandUndone(EmfSessionId clientId, long newStateIndex, long commandId);
	/**
	 * Command was redone.
	 * The topmost command in the redo stack must be redone.
	 * The command Id is sent so client can check that it is still in sync with server.
	 * @param commandId
	 */
	void commandRedone(EmfSessionId clientId, long newStateIndex, long commandId);
	/**
	 * The server responds that the model was saved at the state.
	 * @param savedAtState the state at the server was saved.
	 */
	void modelSaved(final EmfSessionId clientId, final String message, final long savedAtState);
	/**
	 * The model on server side id disposed.
	 * 
	 * Cause may be corrupt model or SVN update.
	 * 
	 * @param sessionId
	 */
	void modelDisposed(EmfSessionId sessionId);
	/**
	 * The server responds that the model was committed at the state.
	 * @param client
	 * @param message
	 * @param savedAtState
	 */
	void modelCommitted(EmfSessionId client, String message, long savedAtState);
}
