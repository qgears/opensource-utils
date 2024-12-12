package hu.qgears.shm.test;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.ECreateType;
import hu.qgears.shm.SharedMemory;
import hu.qgears.shm.SharedMemoryException;
import hu.qgears.shm.UtilSharedMemory;

import org.junit.Assert;
import org.junit.Test;

public class TestShmserver extends TestBase {
	String id = "testShm";
	long size = 10000;

	/**
	 * Tests the shared memory by writing to it and reading from it.
	 * Creates a shared memory instance and two entities with access to it, by
	 * specifying the same shared memory instance identifier: one is writing to
	 * it and the other is reading from it. The data read must be identical to
	 * the data written.
	 * @throws NativeLoadException thrown if native libraries cannot be loaded;
	 * unexpected in this test
	 */
	@Test
	public void testShmServer() throws NativeLoadException {
		SharedMemory mem;
		try {
			mem = UtilSharedMemory.getInstance().createSharedMemory(id,
					ECreateType.createFailsIfExists, size);
		} catch (SharedMemoryException exc) {
			// Delete previous instance
			mem = UtilSharedMemory.getInstance().createSharedMemory(id,
					ECreateType.use, 0);
			mem.deleteSharedMemory();
			mem = UtilSharedMemory.getInstance().createSharedMemory(id,
					ECreateType.createFailsIfExists, size);
		}
		try {
			Assert.assertTrue(size <= mem.getSize());
			Assert.assertNotNull(mem.getJavaAccessor());
			testClient(mem);
		} finally {
			// cleanup
			mem.deleteSharedMemory();
		}
	}

	private void testClient(SharedMemory srv) throws NativeLoadException {
		SharedMemory mem = UtilSharedMemory.getInstance().createSharedMemory(
				id, ECreateType.use, 0);
		Assert.assertTrue(mem.getSize() >= size);
		Assert.assertNotNull(mem.getJavaAccessor());
		Assert.assertFalse(srv.getNativePointer1() == mem.getNativePointer1()
				&& srv.getNativePointer2() == mem.getNativePointer2());
		srv.getJavaAccessor().clear();
		srv.getJavaAccessor().put((byte) 'r');
		srv.getJavaAccessor().put((byte) 'i');
		srv.getJavaAccessor().put((byte) 'z');
		srv.getJavaAccessor().put((byte) 's');
		srv.getJavaAccessor().put((byte) 'i');
		srv.sync(true);
		mem.sync(false);
		mem.getJavaAccessor().clear();
		Assert.assertEquals(mem.getJavaAccessor().get(), (byte) 'r');
		Assert.assertEquals(mem.getJavaAccessor().get(), (byte) 'i');
		Assert.assertEquals(mem.getJavaAccessor().get(), (byte) 'z');
		Assert.assertEquals(mem.getJavaAccessor().get(), (byte) 's');
		Assert.assertEquals(mem.getJavaAccessor().get(), (byte) 'i');
		mem.dispose();
		log("Client read form shared memory the fine values!");
	}
}
