package hu.qgears.remote;

import java.io.Serializable;

public class FilePart implements Serializable {
	private static final long serialVersionUID = 1L;
	public byte[] data;
	public boolean hasMore;
	public long at;
}
