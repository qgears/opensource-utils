package hu.qgears.commons;

import java.util.Stack;
import java.util.concurrent.CancellationException;

/**
 * Create a generic progress counter that counts progress in real numbers
 * as a part of the whole (1.0).
 * 
 * Subtasks created have a name and the current status is the name of the task stack concatenated.
 * 
 * Subtasks are marked finished by closing the created object.
 */
public class ProgressCounter
{
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
	/**
	 * Extended callback interface to update the progress GUI.
	 */
	public interface IProgressCounterHost2 extends IProgressCounterHost
	{
		/**
		 * Called when the progress has finsihed.
		 */
		void progressFinished();
	}
	private volatile boolean cancelled=false;
	private volatile String currentProcessName;
	private volatile double currentProgress;
	private volatile boolean finished=false;
	private IProgressCounterHost host;
	private Stack<ProgressCounterSubTask> tasks=new Stack<ProgressCounterSubTask>();
	private static ThreadLocal<ProgressCounter> threadProgess=new ThreadLocal<ProgressCounter>();
	/**
	 * Create a progress meter.
	 * @param host callback that updates progress GUI. null is allowed
	 * @param name Name of the whole task that is followed by the progress bar.
	 */
	public ProgressCounter(IProgressCounterHost host, String name) {
		this.host=host;
		currentProcessName=name;
		tasks.add(new ProgressCounterSubTask(this, null, name, 1.0));
	}
	public void setCurrent()
	{
		threadProgess.set(this);
	}
	public void close()
	{
		threadProgess.set(null);
		finished=true;
		currentProcessName="Finished";
		currentProgress=1.0;
		if(host instanceof IProgressCounterHost2)
		{
			((IProgressCounterHost2)host).progressFinished();
		}
		host=null;
	}
	public boolean isCancelled()
	{
		return cancelled;
	}
	/**
	 * Throw a {@link CancellationException} in case the current task is cancelled.
	 */
	public void checkCancelled()
	{
		if(cancelled)
		{
			throw new CancellationException("Cancelled by user.");
		}
	}
	public void cancel()
	{
		cancelled=true;
	}
	public static ProgressCounter getCurrent()
	{
		ProgressCounter ret=threadProgess.get();
		if(ret==null)
		{
			return new ProgressCounter(null, "null");
		}
		return ret;
	}
	
	protected void finished(ProgressCounterSubTask subTask) {
		if(tasks.contains(subTask))
		{
			ProgressCounterSubTask st=tasks.pop();
			while(st!=subTask)
			{
				st=tasks.pop();
			}
			ProgressCounterSubTask parent=tasks.peek();
			ProgressCounterSubTask whole=tasks.get(0);
			String currentProcessName=parent.getName();
			double currentProgress=whole.getCurrent();
			this.currentProcessName=currentProcessName;
			this.currentProgress=currentProgress;
			if(host!=null)
			{
				host.setProgressStatus(currentProcessName, currentProgress);
			}
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
		if(host!=null)
		{
			host.setProgressStatus(ret.getName(), whole.getCurrent());
		}
		return ret;
	}
	/**
	 * Get the current progress state of this counter.
	 * @return the current progress state of this counter.
	 */
	public double getCurrentProgress()
	{
		return currentProgress;
	}
	/**
	 * Get the current process name of this counter.
	 * @return the current process name of this counter.
	 */
	public String getCurrentProcessName() {
		return currentProcessName;
	}
	/**
	 * Get the fnished state of this counter.
	 * @return true means this process is finished.
	 */
	public boolean isFinished() {
		return finished;
	}
}
