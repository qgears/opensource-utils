package hu.qgears.shm.test;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.ECreateType;
import hu.qgears.shm.UtilSharedMemory;
import hu.qgears.shm.sem.Semaphore;
import hu.qgears.shm.sem.SemaphoreException;

import org.junit.Assert;
import org.junit.Test;

public class TestSemaphore2 {
	public static void main(String[] args) throws NativeLoadException {
		new TestSemaphore2().testSemaphore();
	}

	String id = "testSem2";

	/**
	 * Scenario test for the {@link Semaphore} class. An OS-level semaphore is 
	 * created and accessed by two threads, testing incrementing, decrementing
	 * primitives, and their timeout-limited variants.
	 * @throws NativeLoadException thrown if required native libraries could not
	 * be loaded - this is unexpected in this test
	 */
	@Test
	public void testSemaphore() throws NativeLoadException {
		Semaphore sem;
		Semaphore sem2;
		try {
			sem = UtilSharedMemory.getInstance().createSemaphore(id,
					ECreateType.createFailsIfExists);
		} catch (SemaphoreException e) {
			sem = UtilSharedMemory.getInstance().createSemaphore(id,
					ECreateType.use);
			sem.deleteSemaphore();
			sem = UtilSharedMemory.getInstance().createSemaphore(id,
					ECreateType.createFailsIfExists);
		}
		sem2 = UtilSharedMemory.getInstance().createSemaphore(id,
				ECreateType.use);
		sem.incrementValue();
		Assert.assertEquals(sem.getValue(), 1);
		sem.incrementValue();
		Assert.assertEquals(sem.getValue(), 2);
		sem2.decrementValue();
		Assert.assertEquals(sem.getValue(), 1);
		sem2.decrementValue();
		Assert.assertEquals(sem.getValue(), 0);
		long t = System.currentTimeMillis();
		OtherThread ot = new OtherThread();
		ot.start();
		sem.decrementValue();
		t = System.currentTimeMillis() - t;
		Assert.assertTrue(t > 980);

		sem.incrementValue();
		Assert.assertTrue(sem.decrementValueTry());
		Assert.assertFalse(sem.decrementValueTry());
		sem.incrementValue();
		Assert.assertTrue(sem.decrementValueTimed(1000));
		t = System.currentTimeMillis();
		Assert.assertFalse(sem.decrementValueTimed(1000));
		t = System.currentTimeMillis() - t;
		Assert.assertTrue(t > 980);
		// cleanup
		sem.deleteSemaphore();
	}

	class OtherThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(1000);
				Semaphore sem = UtilSharedMemory.getInstance().createSemaphore(
						id, ECreateType.use);
				sem.incrementValue();
				sem.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
