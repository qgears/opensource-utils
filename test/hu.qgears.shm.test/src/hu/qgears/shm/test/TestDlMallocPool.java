package hu.qgears.shm.test;

import hu.qgears.commons.mem.DefaultJavaNativeMemory;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.dlmalloc.DlMallocExceptionOutOfMemory;
import hu.qgears.shm.dlmalloc.DlMallocMemory;
import hu.qgears.shm.dlmalloc.DlMallocPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestDlMallocPool {
	private DlMallocPool overusedPool;

	@After
	public void tearDown() {
		if (overusedPool != null) {
			overusedPool.dispose();
		}
	}

	/**
	 * Tests memory dynamic memory allocation algorithm of {@link DlMallocPool} 
	 * a given, preallocated segment: verifies that the algorithm is reusing the
	 * exact location if a subsegment disposed, and a new subsegment is
	 * allocated with the same size.
	 * @throws NativeLoadException if the tested native libraries could not be 
	 * loaded; unexpected in this test
	 */
	@Test
	public void testDlMallocPool() throws NativeLoadException {
		INativeMemory mem = new DefaultJavaNativeMemory(10000);
		DlMallocPool pool = new DlMallocPool(mem, false);
		DlMallocMemory m1 = pool.allocateNativeMemory(100, 0);
		DlMallocMemory m2 = pool.allocateNativeMemory(100, 0);
		long p1 = m2.getNativePointer1();
		long p2 = m2.getNativePointer2();
		m2.dispose();
		DlMallocMemory m3 = pool.allocateNativeMemory(100, 0);
		Assert.assertEquals(p1, m3.getNativePointer1());
		Assert.assertEquals(p2, m3.getNativePointer2());
		Assert.assertTrue(p1 >= m1.getNativePointer1() + 100);
		pool.dispose();
	}

	/**
	 * Verifies that {@link DlMallocPool#allocateNativeMemory(long, int)} throws
	 * {@link DlMallocExceptionOutOfMemory} if there is no free space to serve
	 * the request.
	 */
	@Test(expected = DlMallocExceptionOutOfMemory.class)
	public void testOverallocation() {
		final INativeMemory mem = new DefaultJavaNativeMemory(10000);
		overusedPool = new DlMallocPool(mem, false);

		overusedPool.allocateNativeMemory(100, 0);
		overusedPool.allocateNativeMemory(10000, 0);
	}

}
