package hu.qgears.emfcollab.editor;

import hu.qgears.emfcollab.srv.EmfCommand;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;


/**
 * Command stack that connects the model to an
 * EMFCollab server.
 * 
 * All commands are executed in sync with the remote server.
 * 
 * @author rizsi
 *
 */
public class EmfCollabCommandStack implements CommandStack {
	protected EmfCollabEditorExtension editor;
	private List<EmfCommandWrapper> undoList=new LinkedList<EmfCommandWrapper>();
	public List<EmfCommandWrapper> getUndoList() {
		return undoList;
	}

	private List<EmfCommandWrapper> redoList=new LinkedList<EmfCommandWrapper>();
	public List<EmfCommandWrapper> getRedoList() {
		return redoList;
	}

	private Command mostRecent;
	private List<CommandStackListener> commandStackListeners = new ArrayList<CommandStackListener>();
	
	public EmfCollabCommandStack(EmfCollabEditorExtension editor) {
		super();
		this.editor = editor;
	}

	@Override
	public void addCommandStackListener(CommandStackListener listener) {
		commandStackListeners.add(listener);
	}

	@Override
	public boolean canRedo() {
		return getRedoCommand()!=null;
	}

	@Override
	public boolean canUndo() {
		return getUndoCommand()!=null;
	}

	@Override
	public void execute(Command command) {
		// If the command is executable, record and execute it.
		//
		if (command != null) {
			try
			{
			if (command.canExecute()) {
				if(editor!=null)
				{
					if(!editor.commandAboutToExecute())
					{
						// Command is being executed from remote.
						// Our command has to wait
						editor.scheduleForLaterExecution(command);
						return;
					}
				}
				editor.getExecutor().execute(command);
				EmfCommandWrapper emfCommandWrapper=null;
				if(editor!=null)
				{
					EmfCommand comm=editor.commandExecuted(command.getLabel());
					if(comm!=null)
					{
						emfCommandWrapper=new EmfCommandWrapper(editor.getEmfCommandExecutor(), comm);
					}
				}
				if(emfCommandWrapper==null&&command instanceof EmfCommandWrapper)
				{
					emfCommandWrapper=new EmfCommandWrapper(editor.getEmfCommandExecutor(), ((EmfCommandWrapper)command).getCommand());
				}
				if(emfCommandWrapper!=null)
				{
					undoList.add(emfCommandWrapper);
					mostRecent=emfCommandWrapper;
				}
				// TODO update is save needed flag
				notifyListeners();
			}
			}finally
			{
				command.dispose();
			}
		}
	}
	protected void notifyListeners() {
		for(CommandStackListener li:commandStackListeners)
		{
			li.commandStackChanged(new EventObject(this));
		}
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Command getMostRecentCommand() {
		return mostRecent;
	}

	@Override
	public EmfCommandWrapper getRedoCommand() {
		// TODO only reply the command that was done by this user!
		for(int i=redoList.size()-1;i>=0;--i)
		{
			EmfCommandWrapper c=redoList.get(i);
			if(c.getCommand().getOwner().getId()==editor.getClientId().getId())
			{
				return c;
			}
		}
		return null;
	}

	@Override
	public EmfCommandWrapper getUndoCommand() {
		// TODO only reply the command that was done by this user!
		for(int i=undoList.size()-1;i>=0;--i)
//		for(int i=0;i<undoList.size();++i)
		{
			EmfCommandWrapper c=undoList.get(i);
			if(c.getCommand().getOwner().getId()==editor.getClientId().getId())
			{
				return c;
			}
		}
		return null;
	}

	@Override
	public void redo() {
		EmfCommandWrapper c=getRedoCommand();
		if(c==null)
		{
			return;
		}
		if(editor!=null)
		{
			editor.commandTryRedo(c);
		}
	}
	public void redoCallback(long commandId) {
		EmfCommandWrapper c=getRedoCommand(commandId);
		if(c==null)
		{
			return;
		}
		redoList.remove(c);
		editor.getExecutor().execute(c);
		undoList.add(undoList.size(), c);
		notifyListeners();
	}

	private EmfCommandWrapper getRedoCommand(long commandId) {
		for(EmfCommandWrapper c: redoList)
		{
			if(c.getCommand().getCommandIndex()==commandId)
			{
				return c;
			}
		}
		throw new RuntimeException("Redo error - not found on stack.");
	}

	@Override
	public void removeCommandStackListener(CommandStackListener listener) {
		commandStackListeners.remove(listener);
	}

	@Override
	public void undo() {
		if(undoList.size()<1)
		{
			return;
		}
		EmfCommandWrapper c=getUndoCommand();
		if(editor!=null)
		{
			editor.commandTryUndo(c);
		}
	}
	
	public void undoCallback(long commandId) {
		EmfCommandWrapper c=getUndoCommand(commandId);
		undoList.remove(c);
		editor.getExecutor().undo(c);
		redoList.add(c);
		notifyListeners();
	}
	public void undo(EmfCommandWrapper c)
	{
		if(editor!=null)
		{
			editor.commandTryUndo(c);
		}
	}
	public void redo(EmfCommandWrapper c)
	{
		if(editor!=null)
		{
			editor.commandTryRedo(c);
		}
	}

	private EmfCommandWrapper getUndoCommand(long commandId) {
		for(EmfCommandWrapper c: undoList)
		{
			if(c.getCommand().getCommandIndex()==commandId)
			{
				return c;
			}
		}
		throw new RuntimeException("Undo error - not found on stack.");
	}

	public void setInitialRedoCommands(List<EmfCommand> currentRedoStack) {
		List<EmfCommand> rev=new ArrayList<EmfCommand>(currentRedoStack);
		for(EmfCommand c: rev)
		{
			redoList.add(0, new EmfCommandWrapper(editor.getEmfCommandExecutor(), c));
		}
	}

	public void clearBuffers() {
		undoList.clear();
		redoList.clear();
	}

	public void undoTop() {
		if(undoList.size()<1)
		{
			return;
		}
		EmfCommandWrapper c=undoList.get(undoList.size()-1);
		if(editor!=null)
		{
			editor.commandTryUndo(c);
		}
	}
	public void redoTop() {
		if(redoList.size()<1)
		{
			return;
		}
		EmfCommandWrapper c=redoList.get(redoList.size()-1);
		if(editor!=null)
		{
			editor.commandTryRedo(c);
		}
	}

}
