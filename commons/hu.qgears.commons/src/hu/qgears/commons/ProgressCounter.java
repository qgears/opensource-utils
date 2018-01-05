package hu.qgears.commons;

import java.util.Stack;

/**
 * Create a generic progress counter that counts progress in real numbers
 * as a part of the whole (1.0).
 * 
 * Subtasks created have a name and the current status is the name of the task stack concatenated.
 * 
 * Subtasks are marked finished by closing the created object.
 */
public class ProgressCounter {
	/**
	 * Callback interface to update the progress GUI.
	 */
	public interface IProgressCounterHost
	{
		/**
		 * Update the progress GUI
		 * @param name name of the current task being processed (concatenation of the current task stack)
		 * @param current the current state of the whole progress (in scale [0.0,1.0])
		 */
		void setProgressStatus(String name, double current);
	}
	private IProgressCounterHost host;
	private Stack<ProgressCounterSubTask> tasks=new Stack<ProgressCounterSubTask>();
	/**
	 * Create a progress meter.
	 * @param host callback that updates progress GUI.
	 * @param name Name of the whole task that is followed by the progress bar.
	 */
	public ProgressCounter(IProgressCounterHost host, String name) {
		this.host=host;
		tasks.add(new ProgressCounterSubTask(this, null, name, 1.0));
	}
	
	protected void finished(ProgressCounterSubTask subTask) {
		if(tasks.contains(subTask))
		{
			ProgressCounterSubTask st=tasks.pop();
			while(!subTask.equals(st))
			{
				st=tasks.pop();
			}
			ProgressCounterSubTask parent=tasks.peek();
			ProgressCounterSubTask whole=tasks.get(0);
			host.setProgressStatus(parent.getName(), whole.getCurrent());
		}
	}

	/**
	 * Create a subtask in the progress.
	 * 
	 * It can be used in try/autoclose construct:
	 * 
	 * <pre>
	 * 	try(ProgressCounterSubTask st=pc.subTask("DoThis", 0.5))
	 *  {
	 *    doThis(...);
	 *  }
	 * </pre>
	 * 
	 * @param string name of the subtask
	 * @param d estimated part of the whole parent task
	 * @return the returned subtask must be closed when it was finished
	 */
	public ProgressCounterSubTask subTask(String string, double d) {
		ProgressCounterSubTask parent=tasks.peek();
		ProgressCounterSubTask ret=new ProgressCounterSubTask(this, parent, string, d);
		tasks.push(ret);
		ProgressCounterSubTask whole=tasks.get(0);
		host.setProgressStatus(ret.getName(), whole.getCurrent());
		return ret;
	}
}
