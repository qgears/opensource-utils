package hu.qgears.emfcollab.srv;

import java.util.List;

/**
 * The current (initial from the client's perspective)
 * state of the EMF server.
 * 
 * The starting model can be loaded and already
 * executed commands executed on the client so it gets
 * in sync with the server.
 * @author rizsi
 *
 */
public class EmfInitialState implements EmfSerializable {
	private static final long serialVersionUID = 1L;
	private byte[] xmiFile;
	private long stateIndex;
	private long savedStateIndex;
	public long getSavedStateIndex() {
		return savedStateIndex;
	}
	public void setSavedStateIndex(long savedStateIndex) {
		this.savedStateIndex = savedStateIndex;
	}
	public long getStateIndex() {
		return stateIndex;
	}
	public void setStateIndex(long stateIndex) {
		this.stateIndex = stateIndex;
	}
	private List<EmfCommand> currentUndoStack;
	private List<EmfCommand> currentRedoStack;
	public byte[] getXmiFile() {
		return xmiFile;
	}
	public void setXmiFile(byte[] xmiFile) {
		this.xmiFile = xmiFile;
	}
	public List<EmfCommand> getCurrentUndoStack() {
		return currentUndoStack;
	}
	public void setCurrentUndoStack(List<EmfCommand> currentUndoStack) {
		this.currentUndoStack = currentUndoStack;
	}
	public List<EmfCommand> getCurrentRedoStack() {
		return currentRedoStack;
	}
	public void setCurrentRedoStack(List<EmfCommand> currentRedoStack) {
		this.currentRedoStack = currentRedoStack;
	}
}
