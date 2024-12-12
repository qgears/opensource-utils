package hu.qgears.shm;

import hu.qgears.commons.mem.NativeMemoryException;

public class SharedMemoryException extends NativeMemoryException {
	private static final long serialVersionUID = 1L;

	public SharedMemoryException() {
		super();
	}

	public SharedMemoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public SharedMemoryException(String message) {
		super(message);
	}

	public SharedMemoryException(Throwable cause) {
		super(cause);
	}

}
