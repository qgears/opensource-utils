package hu.qgears.shm.test;

import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.shm.UtilSharedMemory;
import hu.qgears.shm.jmalloc.JMalloc;
import hu.qgears.shm.jmalloc.JMallocPool;
import hu.qgears.shm.jmalloc.JMallocPoolAllocator;

/**
 * Automatic self-tests for {@link JMalloc} implementation.
 */
public class TestJMalloc {
	@BeforeClass
	public static void setup()
	{
		UtilSharedMemory.getInstance();
	}
	private int nPermutationOf3=6;
	@Test
	public void testJMalloc01()
	{
		int align=16;
		JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align*1024*1024, align);
		INativeMemory mem1=m.allocateNativeMemory(112);
		Assert.assertEquals(112, m.getAllAllocated());
		m.selfCheck();
		INativeMemory mem2=m.allocateNativeMemory(113);
		Assert.assertEquals(240, m.getAllAllocated());
		mem1.decrementReferenceCounter();
		m.selfCheck();
		Assert.assertEquals(128, m.getAllAllocated());
		mem2.decrementReferenceCounter();
		mem2.decrementReferenceCounter();	// double free is tolerated.
		m.selfCheck();
		Assert.assertEquals(0, m.getAllAllocated());
	}
	@Test
	public void testJMalloc06FreeBetweenTwoChunks()
	{
		int align=16;
		for(int i=0;i<nPermutationOf3;++i)
		{
			JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align*1024*1024, align);
			INativeMemory mem1=m.allocateNativeMemory(16);
			m.selfCheck();
			INativeMemory mem2=m.allocateNativeMemory(16);
			m.selfCheck();
			INativeMemory mem3=m.allocateNativeMemory(16);
			m.selfCheck();
			freeInEveryOrder(m, new INativeMemory[] {mem1, mem2, mem3}, i);
		}
	}
	@Test
	public void testJMalloc07FreeBetweenTwoChunksFull()
	{
		int align=16;
		for(int i=0;i<nPermutationOf3;++i)
		{
			JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align*3, align);
			INativeMemory mem1=m.allocateNativeMemory(16);
			m.selfCheck();
			INativeMemory mem2=m.allocateNativeMemory(16);
			m.selfCheck();
			INativeMemory mem3=m.allocateNativeMemory(16);
			m.selfCheck();
			freeInEveryOrder(m, new INativeMemory[] {mem1, mem2, mem3}, i);
		}
	}
	@Test
	public void testJMalloc08MultiplePool()
	{
		int align=16;
		for(int i=0;i<nPermutationOf3;++i)
		{
			JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align, align);
			INativeMemory mem1=m.allocateNativeMemory(16);
			m.selfCheck();
			INativeMemory mem2=m.allocateNativeMemory(16);
			m.selfCheck();
			INativeMemory mem3=m.allocateNativeMemory(16);
			m.selfCheck();
			freeInEveryOrder(m, new INativeMemory[] {mem1, mem2, mem3}, i);
		}
	}
	@Test(expected=OutOfMemoryError.class)
	public void testJMalloc08MultiplePoolOOM()
	{
		int align=16;
		JMalloc m=new JMalloc(
				new JMallocPoolAllocator() {
					int n=0;
					@Override
					public JMallocPool allocateNewPool(int poolSize) throws OutOfMemoryError {
						if(n>0)
						{
							throw new OutOfMemoryError("All pools allocated");
						}
						n++;
						return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(poolSize, align));
					}
				}
				, align, align);
		m.allocateNativeMemory(16);
		m.selfCheck();
		m.allocateNativeMemory(16);
	}
	private void freeInEveryOrder(JMalloc host, INativeMemory[] arr, int i) {
		Assert.assertEquals(3, arr.length);
		switch(i)
		{
		case 0:
			arr[0].decrementReferenceCounter();
			host.selfCheck();
			arr[1].decrementReferenceCounter();
			host.selfCheck();
			arr[2].decrementReferenceCounter();
			host.selfCheck();
			break;
		case 1:
			arr[0].decrementReferenceCounter();
			host.selfCheck();
			arr[2].decrementReferenceCounter();
			host.selfCheck();
			arr[1].decrementReferenceCounter();
			host.selfCheck();
			break;
		case 2:
			arr[1].decrementReferenceCounter();
			host.selfCheck();
			arr[0].decrementReferenceCounter();
			host.selfCheck();
			arr[2].decrementReferenceCounter();
			host.selfCheck();
			break;
		case 3:
			arr[1].decrementReferenceCounter();
			host.selfCheck();
			arr[2].decrementReferenceCounter();
			host.selfCheck();
			arr[0].decrementReferenceCounter();
			host.selfCheck();
			break;
		case 4:
			arr[2].decrementReferenceCounter();
			host.selfCheck();
			arr[0].decrementReferenceCounter();
			host.selfCheck();
			arr[1].decrementReferenceCounter();
			host.selfCheck();
			break;
		case 5:
			arr[2].decrementReferenceCounter();
			host.selfCheck();
			arr[1].decrementReferenceCounter();
			host.selfCheck();
			arr[0].decrementReferenceCounter();
			host.selfCheck();
			break;
		}
	}
	@Test(expected = IllegalArgumentException.class)
	public void testJMalloc02AllocateLargerThanPool()
	{
		int align=16;
		JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align*1024*1024, align);
		m.allocateNativeMemory(1024*1025*align);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testJMalloc03AllocateZeroSize()
	{
		int align=16;
		JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align*1024*1024, align);
		m.allocateNativeMemory(0);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testJMalloc04AllocateNegativeSize()
	{
		int align=16;
		JMalloc m=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, align));}, align*1024*1024, align);
		m.allocateNativeMemory(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJMalloc05()
	{
		new JMalloc((size)->{return null;}, 16*1024*1024+1, 16);
	}
	@Test
	public void testJMalloc10() throws InterruptedException, TimeoutException
	{
		JMalloc jm=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, 16));}, 16*1024*1024, 16);
		INativeMemory mem=jm.allocateNativeMemory(128);
		Assert.assertEquals(128, jm.getAllAllocated());
		Assert.assertEquals(128l, mem.getSize());
		mem=null;
		// Garbage collect and wait until finalizer was executed - best effort as it is not possible to be sure
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		long t=System.nanoTime();
		while(jm.getAllAllocated()!=0)
		{
			Thread.sleep(10);
			long t1=System.nanoTime();
			if(t1-t>10000000*500)
			{
				throw new TimeoutException();
			}
		}
	}
	/**
	 * Test case when allocated chunks are not multiple of 16.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	@Test
	public void testJMalloc11() throws InterruptedException, TimeoutException
	{
		JMalloc jm=new JMalloc((size)->{return new JMallocPool(DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(size, 16));}, 16*1024*1024, 16);
		INativeMemory mem=jm.allocateNativeMemory(127);
		INativeMemory mem2=jm.allocateNativeMemory(127);
		Assert.assertEquals(256, jm.getAllAllocated());
		Assert.assertEquals(127l, mem.getSize());
		Assert.assertEquals(127l, mem2.getSize());
	}
}
