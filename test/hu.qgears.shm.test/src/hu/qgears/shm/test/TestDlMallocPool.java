package hu.qgears.shm.test;

import hu.qgears.commons.mem.DefaultJavaNativeMemory;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.dlmalloc.DlMallocExceptionOutOfMemory;
import hu.qgears.shm.dlmalloc.DlMallocMemory;
import hu.qgears.shm.dlmalloc.DlMallocPool;

import org.junit.Assert;
import org.junit.Test;


public class TestDlMallocPool {
	@Test
	public void testDlMallocPool() throws NativeLoadException
	{
		INativeMemory mem=new DefaultJavaNativeMemory(10000);
		DlMallocPool pool=new DlMallocPool(mem, false);
		DlMallocMemory m1=pool.allocateNativeMemory(100, 0);
		DlMallocMemory m2=pool.allocateNativeMemory(100, 0);
		long p1=m2.getNativePointer1();
		long p2=m2.getNativePointer2();
		m2.dispose();
		DlMallocMemory m3=pool.allocateNativeMemory(100, 0);
		Assert.assertEquals(p1, m3.getNativePointer1());
		Assert.assertEquals(p2, m3.getNativePointer2());
		Assert.assertTrue(p1>=m1.getNativePointer1()+100);
		boolean thrown=false;
		try
		{
			pool.allocateNativeMemory(10000, 0);
		}catch(DlMallocExceptionOutOfMemory e)
		{
			thrown=true;
		}
		Assert.assertTrue(thrown);
		pool.dispose();
	}
}
