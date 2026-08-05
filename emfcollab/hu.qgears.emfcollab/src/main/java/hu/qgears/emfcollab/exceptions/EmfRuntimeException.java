package hu.qgears.emfcollab.exceptions;

public class EmfRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EmfRuntimeException() {
		super();
	}

	public EmfRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmfRuntimeException(String message) {
		super(message);
	}

	public EmfRuntimeException(Throwable cause) {
		super(cause);
	}

}
