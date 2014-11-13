package hu.qgears.commons;

/**
 * Reference counter implementation.
 * 
 * Reference counter is set to 1 on creation.
 * @author rizsi
 *
 */
abstract public class AbstractReferenceCountedDisposeable implements IReferenceCountedDisposeable {
	private volatile int references=1;
	@Override
	public  void incrementReferenceCounter() {
		references++;
	}
	@Override
	public void decrementReferenceCounter()
	{
		references--;
		if(references<1)
		{
			dispose();
		}
	}
	public int getReferenceCounter() {
		return references;
	}
	private boolean disposed=false;
	@Override
	public boolean isDisposed() {
		return disposed;
	}
	@Override
	final public synchronized void dispose() {
		if(!disposed)
		{
			singleDispose();
			disposed=true;
		}
	}
	protected void markDisposed()
	{
		disposed=true;
	}
	/**
	 * Implement dispose logic here. It is guaranteed to be called only once.
	 * It is run in a synchronized block synced to the host obejct.
	 */
	abstract protected void singleDispose();
	/**
	 * Throw exception in case the object is already disposed.
	 */
	protected void checkDisposed() {
		if(isDisposed())
		{
			throw new RuntimeException("Object already disposed");
		}
	}
}
