package hu.qgears.shm.test;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.ECreateType;
import hu.qgears.shm.UtilSharedMemory;
import hu.qgears.shm.sem.Semaphore;
import hu.qgears.shm.sem.SemaphoreException;
import junit.framework.Assert;

import org.junit.Test;


public class TestSemaphore extends TestBase {
	public static void main(String[] args) throws NativeLoadException {
		new TestSemaphore().testSemaphore();
	}
	String id="testSem";
	@Test
	public void testSemaphore() throws NativeLoadException
	{
		Semaphore sem;
		try
		{
			sem=UtilSharedMemory.getInstance().createSemaphore(id, ECreateType.createFailsIfExists);
		}catch(SemaphoreException e)
		{
			sem=UtilSharedMemory.getInstance().createSemaphore(id, ECreateType.use);
			sem.deleteSemaphore();
			sem=UtilSharedMemory.getInstance().createSemaphore(id, ECreateType.createFailsIfExists);
		}
		log("Semaphore created: "+id);
		sem.incrementValue();
		Assert.assertEquals(sem.getValue(), 1);
		log("Semaphore incremented and value is 1");
		sem.incrementValue();
		Assert.assertEquals(sem.getValue(), 2);
		log("Semaphore incremented and value is 2");
		sem.decrementValue();
		Assert.assertEquals(sem.getValue(), 1);
		log("Semaphore decremented and value is 1");
		sem.decrementValue();
		log("Semaphore decremented");
		Assert.assertEquals(sem.getValue(), 0);
		log("... and value is 0");
		long t=System.currentTimeMillis();
		OtherThread ot=new OtherThread();
		ot.start();
		log("Waiting for other thread to increment...");
		sem.decrementValue();
		t=System.currentTimeMillis()-t;
		Assert.assertTrue(t>980);
		log("... one second wait is fine");
		
		sem.incrementValue();
		Assert.assertTrue(sem.decrementValueTry());
		log("decrement try when positive is fine");
		Assert.assertFalse(sem.decrementValueTry());
		log("decrement try when zero is fine");
		sem.incrementValue();
		Assert.assertTrue(sem.decrementValueTimed(1000));
		t=System.currentTimeMillis();
		Assert.assertFalse(sem.decrementValueTimed(1000));
		t=System.currentTimeMillis()-t;
		Assert.assertTrue("try decrement with wait for one second", t>980);
		log("timed decrement is fine");
		// cleanup
		sem.deleteSemaphore();
		log("sem deleted, test case ready");
	}
	class OtherThread extends Thread
	{
		@Override
		public void run() {
			try {
				log("other thread started. sleep a second...");
				Thread.sleep(1000);
				log("create a new reference to semaphore...");
				Semaphore sem=UtilSharedMemory.getInstance().createSemaphore(id, ECreateType.use);
				log("increment semaphore...");
				sem.incrementValue();
				log("semaphore incremented from other thread");
				sem.dispose();
				log("semaphore refernece disposed on other thread");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
