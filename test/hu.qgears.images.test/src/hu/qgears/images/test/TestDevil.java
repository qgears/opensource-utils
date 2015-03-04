package hu.qgears.images.test;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilMemory;
import hu.qgears.images.devil.NativeDevIL;
import hu.qgears.images.devil.NativeDevILManager;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;


public class TestDevil {

	/*
	 * TODO Fix this test case, remove endless loop
	 * */
	@Test@Ignore
	public void testMemoryLeak() throws FileNotFoundException, Throwable
	{
		int N=2;
		NativeDevILManager.getInstance();
		Thread.sleep(1000);
		byte[] content=UtilFile.loadFile(getClass().getResource("1292.png"));
		byte[] content2=UtilFile.loadFile(getClass().getResource("1296.png"));
		NativeDevIL n=NativeDevILManager.getInstance().createDevIL();
		UtilMemory.printMemoryUsed("before test");
		for(int i=0;i<N;++i)
		{
			System.out.println("--- load 1");
			n.load(content, ".png");
			System.out.println("size: "+n.getWidth()*n.getHeight()*3);
			System.out.println("--- load 2");
			n.dispose();
			n=NativeDevILManager.getInstance().createDevIL();
			n.load(content2, ".png");
			System.out.println("size: "+n.getWidth()*n.getHeight()*3);
			n.dispose();
			n=NativeDevILManager.getInstance().createDevIL();
//			n.copyBuffer();
//			n.dispose();
//			System.gc();
		}
		UtilMemory.printMemoryUsed("after test");
		while(true)
		{
			Thread.sleep(1000);
			System.gc();
		}
	}
	public static void main(String[] args) throws FileNotFoundException, Throwable {
		new TestDevil().testMemoryLeak();
	}
}
