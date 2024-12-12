package hu.qgears.shm;

/**
 * Enumeration constants for creating a shared memory accessor.
 * 
 * It is platform specific which modes are implemented fine.
 * @author rizsi
 *
 */
public enum ECreateType {
	/**
	 * Create the shared memory object or use in case it already exists.
	 */
//	createUseIfExists,
	/**
	 * Create the shared memory object. Fail in case it already exists.
	 */
	createFailsIfExists,
	/**
	 * Use the already existing shared memory object.
	 * Size is determined from the existing shared memory.
	 */
	use,
	/**
	 * Delete if exists and re-create. Fail in case could not be deleted.
	 */
	deleteAndCreate,
	/**
	 * Delete the semaphor at once. Object goes to disposed state at once.
	 */
	delete,
}
