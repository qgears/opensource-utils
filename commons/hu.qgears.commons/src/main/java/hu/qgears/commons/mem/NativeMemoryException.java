package hu.qgears.commons.mem;

public class NativeMemoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NativeMemoryException() {
		super();
	}

	public NativeMemoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public NativeMemoryException(String message) {
		super(message);
	}

	public NativeMemoryException(Throwable cause) {
		super(cause);
	}

}
