package hu.qgears.emfcollab.editor;

import org.eclipse.emf.common.command.Command;

public class SimpleCommandExecutor implements ICommandExecutor
{

	@Override
	public void execute(Command command) {
		command.execute();
	}

	@Override
	public void undo(Command c) {
		c.undo();
	}

}
