package hu.qgears.shm.sem;

public class SemaphoreException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SemaphoreException() {
		super();
	}

	public SemaphoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public SemaphoreException(String message) {
		super(message);
	}

	public SemaphoreException(Throwable cause) {
		super(cause);
	}
}
