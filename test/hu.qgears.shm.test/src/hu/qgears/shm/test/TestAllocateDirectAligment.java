package hu.qgears.shm.test;

import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.mem.INativeMemory;
import hu.qgears.shm.UtilSharedMemory;
import hu.qgears.shm.part.PartNativeMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test that Java allocated direct memory must be 16 byte aligned.
 * @author rizsi
 *
 */
public class TestAllocateDirectAligment {
	@Test
	public void testAllocationAlignment()
	{
		UtilSharedMemory.getInstance();
		Random r=new Random();
		List<INativeMemory> mems=new ArrayList<INativeMemory>();
		for(int i=0;i<10000;++i)
		{
			INativeMemory mem=DefaultJavaNativeMemoryAllocator.getInstance().allocateNativeMemory(r.nextInt(1009)+1);
			mems.add(mem);
			PartNativeMemory pnm=new PartNativeMemory(mem, 0, 1);
			Assert.assertTrue("Java allocated direct memory must be 16 byte aligned.", pnm.getNativePointer1()%16==0);
		}
	}
}
