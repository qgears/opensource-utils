package hu.qgears.emfcollab.impl;

import hu.qgears.emfcollab.srv.EmfCommand;

import java.util.ArrayList;
import java.util.List;


public class ResourceWithHistory {
	private static final long serialVersionUID = 1L;
	private List<EmfCommand> undoList;
	private List<EmfCommand> redoList;
	private LoadedResource resource;
	public ResourceHistory getHistory()
	{
		return new ResourceHistory(0, 0, undoList, redoList);
	}
	public ResourceWithHistory(LoadedResource resource,
			List<EmfCommand> undoList, List<EmfCommand> redoList) {
		super();
		this.resource = resource;
		this.undoList = undoList;
		this.redoList = redoList;
	}
	public ResourceWithHistory(LoadedResource r, ResourceHistory history) {
		this.resource = r;
		if(history==null)
		{
			undoList=new ArrayList<EmfCommand>();
			redoList=new ArrayList<EmfCommand>();
		}else
		{
			undoList=history.getUndoList();
			redoList=history.getRedoList();
		}
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
	public LoadedResource getResource() {
		return resource;
	}
	public void setResource(LoadedResource resource) {
		this.resource = resource;
	}
	
}
