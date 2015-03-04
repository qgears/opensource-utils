package hu.qgears.opengl.mirgl;

public class MirGlException extends Exception
{
	private static final long serialVersionUID = 1L;

	public MirGlException() {
		super();
	}

	public MirGlException(String message, Throwable cause) {
		super(message, cause);
	}

	public MirGlException(String message) {
		super(message);
	}

	public MirGlException(Throwable cause) {
		super(cause);
	}
}
