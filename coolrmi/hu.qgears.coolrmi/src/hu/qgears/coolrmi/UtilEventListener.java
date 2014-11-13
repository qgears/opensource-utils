package hu.qgears.coolrmi;

/**
 * Listener interface for <code>UtilEvent</code>s 
 * @author rizsi
 *
 */

public interface UtilEventListener<T> {
	/**
	 * Method is called when the event is fired.
	 * @param msg the parameter of the event. Its content must be specified
	 * by the event's definition.
	 */
	void eventHappened(T msg);
}
