package hu.qgears.coolrmi;

/**
 * Interface to connect a logger that logs protocol errors.
 * The default logger logs to the syserr. 
 */
public interface ICoolRMILogger {
	void logError(Throwable e);
}
