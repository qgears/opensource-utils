package hu.qgears.nativeloader;

/**
 * Exception occured while loading native component.
 * @author rizsi
 *
 */
public class NativeLoadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NativeLoadException() {
		super();
	}

	public NativeLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public NativeLoadException(String message) {
		super(message);
	}

	public NativeLoadException(Throwable cause) {
		super(cause);
	}	
}
