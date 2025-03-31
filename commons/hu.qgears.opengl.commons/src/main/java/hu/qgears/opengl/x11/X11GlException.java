package hu.qgears.opengl.x11;

public class X11GlException extends Exception
{
	private static final long serialVersionUID = 1L;

	public X11GlException() {
		super();
	}

	public X11GlException(String message, Throwable cause) {
		super(message, cause);
	}

	public X11GlException(String message) {
		super(message);
	}

	public X11GlException(Throwable cause) {
		super(cause);
	}
}
