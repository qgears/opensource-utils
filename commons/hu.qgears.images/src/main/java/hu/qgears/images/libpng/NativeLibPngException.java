package hu.qgears.images.libpng;

public class NativeLibPngException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NativeLibPngException() {
		super();
	}

	public NativeLibPngException(String message, Throwable cause) {
		super(message, cause);
	}

	public NativeLibPngException(String message) {
		super(message);
	}

	public NativeLibPngException(Throwable cause) {
		super(cause);
	}

}
