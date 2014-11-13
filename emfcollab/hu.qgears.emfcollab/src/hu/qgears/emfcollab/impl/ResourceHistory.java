package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.srv.EmfCommand;
import hu.qgears.emfcollab.srv.EmfSerializable;

import java.util.List;


/**
 * Undo and redo stack of the resource.
 * @author rizsi
 *
 */
public class ResourceHistory implements EmfSerializable {
	private static final long serialVersionUID = 1L;
	private long firstState;
	private long lastState;
	private List<EmfCommand> undoList;
	private List<EmfCommand> redoList;
	public ResourceHistory(long firstState, long lastState,
			List<EmfCommand> undoList, List<EmfCommand> redoList) {
		super();
		this.firstState = firstState;
		this.lastState = lastState;
		this.undoList = undoList;
		this.redoList = redoList;
	}
	public long getFirstState() {
		return firstState;
	}
	public void setFirstState(long firstState) {
		this.firstState = firstState;
	}
	public long getLastState() {
		return lastState;
	}
	public void setLastState(long lastState) {
		this.lastState = lastState;
	}
	public List<EmfCommand> getUndoList() {
		return undoList;
	}
	public void setUndoList(List<EmfCommand> undoList) {
		this.undoList = undoList;
	}
	public List<EmfCommand> getRedoList() {
		return redoList;
	}
	public void setRedoList(List<EmfCommand> redoList) {
		this.redoList = redoList;
	}
	
}
