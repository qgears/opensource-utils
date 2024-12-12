package hu.qgears.shm.dlmalloc;


public class DlMallocExceptionOutOfMemory extends DlMallocException {
	private static final long serialVersionUID = 1L;

	public DlMallocExceptionOutOfMemory() {
		super();
	}

	public DlMallocExceptionOutOfMemory(String message, Throwable cause) {
		super(message, cause);
	}

	public DlMallocExceptionOutOfMemory(String message) {
		super(message);
	}

	public DlMallocExceptionOutOfMemory(Throwable cause) {
		super(cause);
	}

}
