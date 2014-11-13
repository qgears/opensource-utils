package hu.qgears.emfcollab.editor;

import org.eclipse.emf.common.command.Command;

/**
 * This interface is used to specify the command
 * executor for EMF commands.
 * EG the executor may require to create an
 * embedding operation or so.
 * @author rizsi
 *
 */
public interface ICommandExecutor {

	void execute(Command command);

	void undo(Command c);

}
