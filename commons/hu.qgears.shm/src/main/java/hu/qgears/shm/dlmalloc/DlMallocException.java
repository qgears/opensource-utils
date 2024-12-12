package hu.qgears.shm.dlmalloc;

import hu.qgears.commons.mem.NativeMemoryException;

public class DlMallocException extends NativeMemoryException {
	private static final long serialVersionUID = 1L;

	public DlMallocException() {
		super();
	}

	public DlMallocException(String message, Throwable cause) {
		super(message, cause);
	}

	public DlMallocException(String message) {
		super(message);
	}

	public DlMallocException(Throwable cause) {
		super(cause);
	}

}
