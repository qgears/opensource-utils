package hu.qgears.shm.sem;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;
import hu.qgears.shm.ECreateType;


/**
 * Semaphore JNI wrapper.
 * @author rizsi
 *
 */
public class Semaphore extends AbstractReferenceCountedDisposeable {
	SemaphoreNative nat;
	/**
	 * Create an inter-process semaphore object.
	 * Use UtilSharedMemory.createSemaphore instead!
	 * @param id global id of the semaphore object to be created.
	 * @param createType how to create the semaphore.
	 */
	public Semaphore(String id, ECreateType createType) {
		nat=new SemaphoreNative();
		nat.init(id, createType.ordinal());
		if(ECreateType.delete.equals(createType))
		{
			markDisposed();
		}
	}

	public void deleteSemaphore()
	{
		checkDisposed();
		nat.nativeDispose(true);
		markDisposed();
	}

	@Override
	protected void singleDispose() {
		nat.nativeDispose(false);
	}
	public int getValue()
	{
		checkDisposed();
		return nat.getValue();
	}
	public void incrementValue()
	{
		checkDisposed();
		nat.incrementValue();
	}
	/**
	 * Decrement value - block until semaphore is positive.
	 */
	public void decrementValue()
	{
		checkDisposed();
		nat.decrementValue();
	}
	/**
	 * Try to decrement value
	 * @return true if decrementing was succesful
	 */
	public boolean decrementValueTry()
	{
		checkDisposed();
		return nat.decrementValueTry();
	}
	/**
	 * 
	 * @param timeoutMillis timeout in millis - (relative to now)
	 * @return true if decrementing was succesful within timeout
	 */
	public boolean decrementValueTimed(long timeoutMillis)
	{
		checkDisposed();
		return nat.decrementValueTimed(timeoutMillis, System.currentTimeMillis()+timeoutMillis);
	}
}
