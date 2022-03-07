package hu.qgears.parser.util;

import hu.qgears.parser.ITreeElem;

public class ParseRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public final ITreeElem tree;
	public ParseRuntimeException(ITreeElem t, String string) {
		super(string);
		this.tree=t;
	}
	public ParseRuntimeException(String string) {
		super(string);
		tree=null;
	}
}
