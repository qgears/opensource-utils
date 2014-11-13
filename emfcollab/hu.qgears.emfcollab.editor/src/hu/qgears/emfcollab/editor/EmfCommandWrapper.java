package hu.qgears.emfcollab.editor;

import hu.qgears.emfcollab.EmfCommandExecutor;
import hu.qgears.emfcollab.srv.EmfCommand;

import org.eclipse.emf.common.command.AbstractCommand;


/**
 * Wraps a serializable EmfCommand object (part of EMFCollab)
 * into an org.eclipse.emf.common.command.Command object
 * @author rizsi
 *
 */
public class EmfCommandWrapper extends AbstractCommand {
	EmfCommandExecutor executor;
	EmfCommand command;
	
	public EmfCommand getCommand() {
		return command;
	}

	public EmfCommandWrapper(EmfCommandExecutor executor, EmfCommand command) {
		super();
		this.executor = executor;
		this.command = command;
		setLabel(command.getName());
	}

	@Override
	public void execute() {
		executor.executeEvents(command.getEvents());
	}

	@Override
	public void redo() {
		executor.executeEvents(command.getEvents());
	}
	@Override
	protected boolean prepare() {
		return true;
	}
	
	@Override
	public void undo() {
		executor.undoEvents(command.getEvents());
	}

	@Override
	public boolean canUndo() {
		return true;
	}

}
