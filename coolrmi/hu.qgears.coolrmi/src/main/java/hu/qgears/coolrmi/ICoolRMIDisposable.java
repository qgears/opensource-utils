package hu.qgears.coolrmi;

/**
 * Marker interface that this object has to be disposed
 * when the connection is closed.
 */
public interface ICoolRMIDisposable {
	void disposeWhenDisconnected();
}
