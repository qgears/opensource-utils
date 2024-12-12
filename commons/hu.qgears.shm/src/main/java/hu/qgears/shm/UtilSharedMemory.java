package hu.qgears.shm;

import java.io.File;

import org.apache.log4j.Logger;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.shm.natives.Accessor;
import hu.qgears.shm.sem.Semaphore;


/**
 * Accessor to shared memory instances.
 * Singleton - loads native code on first getInstance()
 * @author rizsi
 *
 */
public class UtilSharedMemory {
	
	private static final Logger LOG = Logger.getLogger(UtilSharedMemory.class);

	private static UtilSharedMemory instance;
	private UtilSharedMemory() throws NativeLoadException
	{
		UtilNativeLoader.loadNatives(new  Accessor());
	}
	/**
	 * Loads native libraries required by this bundle.
	 * Must be called once before using any classes in this bundle.
	 * Multiple calls don't cause problem.
	 * @return
	 * @throws NativeLoadException
	 */
	public synchronized static UtilSharedMemory getInstance() throws NativeLoadException {
		if(instance==null)
		{
			instance=new UtilSharedMemory();
		}
		return instance;
	}

	/**
	 * Create a shared memory pool accessor object.
	 * @param id id that is used to connect shared memory instances through processes
	 * 	(the same id references the same shared memory - implementation is platform dependent)
	 * @param createType whether to create or access an existing shared memory object
	 * @param size the requested size of the shared memory. In case the shared memory exists this parameter is not used. The size of the memory can be queried from the object returned.
	 * @return accessor to the shared memory created or accessed
	 */
	public SharedMemory createSharedMemory(String id, ECreateType createType, long size)
	{
		return new SharedMemory(id, createType, size);
	}
	/**
	 * Open a file with memory mapping: the whole file is mapped into a
	 * direct buffer.
	 * @param f the file to be opened and mapped into memory
	 * @return accessor to the memory map created
	 */
	public SharedMemory openSharedFile(File f)
	{
		return new SharedMemory(f);
	}
	/**
	 * Open a shared memory object by id.
	 * This entry point is implemented on both Windows and Linux.
	 * @param id
	 * @return
	 */
	public SharedMemory openSharedMemoryById(long id)
	{
		return new SharedMemory(id);
	}
	public void deleteSharedMemoryById(long id) {
		new SharedMemoryNative().deleteSharedMemoryById(id);
	}
	/**
	 * Create a semaphore instance
	 * @param id
	 * @param createType
	 * @return
	 */
	public Semaphore createSemaphore(String id, ECreateType createType)
	{
		return new Semaphore(id, createType);
	}
	/**
	 * Delete an existing semaphore.
	 * @param id
	 */
	public void deleteSemaphore(String id)
	{
		try {
			Semaphore sem=createSemaphore(id, ECreateType.use);
			sem.deleteSemaphore();
		} catch (Exception e) {
			//ignore exception if semaphore is already deleted
			LOG.warn("Attempt to delete an already deleted semaphore",e);
		}
	}
}
