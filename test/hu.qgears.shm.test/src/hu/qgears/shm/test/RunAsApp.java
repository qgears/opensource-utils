package hu.qgears.shm.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Run all tests as a standalone application.
 * @author rizsi
 *
 */
public class RunAsApp {
	public static void main(String[] args) {
		Result res=JUnitCore.runClasses(
				TestDlMallocPool.class,
				TestSemaphore.class,
				TestShmserver.class
				);
		new MyResultPrinter(System.out).print(res);
	}
}
