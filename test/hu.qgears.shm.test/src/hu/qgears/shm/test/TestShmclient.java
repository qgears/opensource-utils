package hu.qgears.shm.test;

import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.shm.ECreateType;
import hu.qgears.shm.UtilSharedMemory;


public class TestShmclient {
	public static void main(String[] args) throws NativeLoadException {
		UtilSharedMemory.getInstance().createSharedMemory("cica", ECreateType.use, 1024*1024*1024);
	}
}
