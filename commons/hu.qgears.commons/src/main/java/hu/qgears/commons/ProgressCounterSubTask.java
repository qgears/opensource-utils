package hu.qgears.commons;

/**
 * A sub task of the simple progress bar progress counting subsystem.
 * When the subtask is finished the close method has to be called.
 */
public class ProgressCounterSubTask implements AutoCloseable
{
	private ProgressCounter context;
	private ProgressCounterSubTask parent;
	private String name;
	private String simpleName;
	/**
	 * Parent was at this status when this subtask was started.
	 */
	private double parentStartAt;
	/**
	 * This subtask is this part of its parent in [0.0,1.0] scale.
	 */
	private double all;
	/**
	 * Current status of this task on [0.0,1.0] scale.
	 */
	private double current;
	/**
	 * Timestamp when this progress item was created.
	 */
	public final long createdAtNanoTime=System.nanoTime();
	public ProgressCounterSubTask(ProgressCounter context, ProgressCounterSubTask parent, String name, double all) {
		this.context=context;
		this.parent=parent;
		this.simpleName=name;
		if(parent!=null)
		{
			this.parentStartAt=parent.current;
			this.name=parent.name+"/"+name;
		}else
		{
			this.name=name;
			this.parentStartAt=0;
		}
		this.all=all;
	}

	@Override
	public void close() {
		parent.setWork(parentStartAt+all);
		context.finished(this);
	}
	/**
	 * Set the current progress status.
	 * @param work
	 */
	public void setWork(double work) {
		current=work;
		if(current<0)
		{
			current=0;
		}
		if(current>1.0)
		{
			current=1.0;
		}
		if(parent!=null)
		{
			parent.setWork(parentStartAt+all*current);
		}else
		{
			context.setWork(parentStartAt+all*current);
		}
	}
	/**
	 * Get the full name of this task (including parent names)
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * Get the local simple name of this task.
	 * @return
	 */
	public String getSimpleName() {
		return simpleName;
	}
	/**
	 * Get the current progress status of this task.
	 * @return
	 */
	public double getCurrent() {
		return current;
	}
	/**
	 * Get the parent task.
	 * @return
	 */
	public ProgressCounterSubTask getParent() {
		return parent;
	}
	@Override
	public String toString() {
		return name+": "+current;
	}
}
