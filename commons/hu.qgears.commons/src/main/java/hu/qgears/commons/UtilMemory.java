package hu.qgears.commons;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

public class UtilMemory {

	private static final Logger LOG = Logger.getLogger(UtilMemory.class);
	/**
	 * Snapshot of JVM memory status.
	 */
	public static class MemoryStatus
	{
		/**
		 * Time of two GCs in nanoseconds.
		 */
		public final long gcNanos;
		/**
		 * Size of total memory in bytes.
		 */
		public final long totalMemory;
		/**
		 * Size of free memory in bytes.
		 */
		public final long freeMemory;
		/**
		 * Create JVM memory state snapshot object.
		 * @param gcNanos Time of two GCs in nanoseconds before this snapshot.
		 */
		public MemoryStatus(long gcNanos) {
			this.gcNanos=gcNanos;
			totalMemory = Runtime.getRuntime().totalMemory();
			freeMemory = Runtime.getRuntime().freeMemory();
		}
		/**
		 * Size of allocated memory in bytes.
		 * @return
		 */
		public long getAllocated()
		{
			return totalMemory-freeMemory;
		}
		@Override
		public String toString() {
			return "allocated: "+getAllocated()+" 2xGC nanos: "+gcNanos;
		}
	}
	
	private UtilMemory() {
		// disable constructor of utility class
	}

	/**
	 * Generates load for garbage collector measurements
	 * @author rizsi
	 *
	 */
	public static class ArtificialLoadForGC extends Thread
	{
		/**
		 * 
		 * @param n objects created per round (1000)
		 * @param max bunch of objects kept (200)
		 * @param FPS rounds per second (60)
		 */
		public ArtificialLoadForGC(int n, int max,int FPS) {
			setDaemon(true);
			this.n=n;
			this.max=max;
			this.FPS=FPS;
		}
		public void run() {
			while(true)
			{
				createManyStupidObjects();
				try {
					Thread.sleep(1000/FPS);
				} catch (InterruptedException e) {
					LOG.error("Interrupted",e);
				}
			}
		};
		List<Set<String>> dummyList=new ArrayList<Set<String>>();
		List<Set<String>> oldList=new ArrayList<Set<String>>();
		private int n=1000;
		private int max=200;
		private int FPS=60;
		int idx=0;
		/**
		 * Create dummy objects. and delete them sometimes.
		 * Must be called periodically.
		 */
		public void createManyStupidObjects() {
			Set<String> mystrings=new HashSet<String>();
			// No crypto functions are required here
			@SuppressWarnings({ "squid:S2245", "findsecbugs:PREDICTABLE_RANDOM" })
			String prefix=""+new Random().nextInt();
			for(int i=0;i<n;++i)
			{
				mystrings.add(prefix+i);
			}
			dummyList.add(mystrings);
			if(idx%max==0)
			{
				oldList=dummyList;
				dummyList=new ArrayList<Set<String>>();
			}
		}
	}
	public static void printMemoryUsed(String where) {
		MemoryStatus status=getMemoryUsed();
		LOG.info("Memory used: " + status + " at: " + where);
	}
	/**
	 * Do two garbage collections and create a JSM memory status object.
	 * Use for debugging purposes only!
	 * @return
	 */
	public static MemoryStatus getMemoryUsed() {
		long t0=System.nanoTime();
		System.gc();
		System.gc();
		t0=System.nanoTime()-t0;
		MemoryStatus status=new MemoryStatus(t0);
		return status;
	}

	/**
	 * Garbage collector types that are implemented in JVMs
	 * 
	 * @author rizsi
	 * 
	 */
	public static enum GCTypes {
		UseSerialGC, UseParallelGC, UseConcMarkSweepGC, UseParNewGC, UseParallelOldGC, unknown,
	}

	/**
	 * Query which GC we use.
	 * 
	 * WARNING: implementation depends on specific VM! (Implemented on
	 * openjdk-6)
	 * 
	 * @return the type of GC used in this VM
	 * 
	 */
	public static GCTypes queryGCType() {
		GCTypes ret = GCTypes.unknown;
		try {
			List<GarbageCollectorMXBean> gcms = ManagementFactory
					.getGarbageCollectorMXBeans();
			List<String> names = new ArrayList<String>();
			for (GarbageCollectorMXBean gcm : gcms) {
				names.add(gcm.getName());
			}
			Collections.sort(names);
			if (names.size() == 2 && names.contains("ParNew")
					&& names.contains("ConcurrentMarkSweep")) {
				ret = GCTypes.UseConcMarkSweepGC;
			}
			if (names.size() == 2 && names.contains("Copy")
					&& names.contains("MarkSweepCompact")) {
				ret = GCTypes.UseSerialGC;
			}
			if (names.size() == 2 && names.contains("PS Scavenge")
					&& names.contains("PS MarkSweep")) {
				ret = GCTypes.UseParallelGC;
			}
			if (names.size() == 2 && names.contains("ParNew")
					&& names.contains("MarkSweepCompact")) {
				ret = GCTypes.UseParNewGC;
			}
		} catch (Throwable e) {
			LOG.error("GC",e);
		}
		return ret;
	}
	/**
	 * Checks whether the VM GC is low latency.
	 * Throws exception when not!
	 * @throws RuntimeException
	 */
	public static void requiresLowLatencyGC() throws RuntimeException
	{
		if(!GCTypes.UseConcMarkSweepGC.equals(queryGCType()))
		{
			throw new RuntimeException("Low latency GC is required! Use: -XX:+UseConcMarkSweepGC");
		}
	}
	/**
	 * Checks whether the VM GC is low latency.
	 * Logs to System.err when not
	 */
	public static void warnRequiresLowLatencyGC()
	{
		if(!GCTypes.UseConcMarkSweepGC.equals(queryGCType()))
		{
			LOG.error("Low latency GC is required! Use: -XX:+UseConcMarkSweepGC");
		}
	}
	private static Thread gcThread;
	private static ForceGcRunnable forceGc;
	private static class ForceGcRunnable implements Runnable
	{
		private long pause;
		GCLogger log;
		public ForceGcRunnable(long pause,  GCLogger log) {
			super();
			this.pause = pause;
			this.log=log;
		}
		@Override
		public void run() {
			while(true)
			{
				if(log!=null)
				{
					log.before();
				}
				System.gc();
				try {
					if(log!=null)
					{
						log.after();
					}
					Thread.sleep(pause);
				} catch (InterruptedException e) {
					LOG.error("Interrupt",e);
				}
			}
		}
		
	}
	public interface GCLogger
	{
		void before();
		void after();
	}
	public static class GCLoggerImpl implements GCLogger
	{
		long t;
		@Override
		public void before() {
			t=System.currentTimeMillis();
		}

		@Override
		public void after() {
			long diff=System.currentTimeMillis()-t;
			LOG.info("GC at: "+t+" lasts: "+diff+" (millis)");
		}
		
	}
	/**
	 * Start a thread that forces garbage collection on a regular basis.
	 * @param pause
	 */
	public static void startGcThread(long pause)
	{
		startGcThread(pause, null);
	}

	/**
	 * Start a thread that forces garbage collection on a regular basis. This
	 * call is not designed to us in multithread environment!
	 * 
	 * @param pause
	 * @param log
	 *            callback that is called before and after garbage collection.
	 *            May be null.
	 */
	public static void startGcThread(long pause, GCLogger log) {
		//this class is should not be used in multihread env
		if (gcThread == null) {//NOSONAR
			forceGc = new ForceGcRunnable(pause, log);
			gcThread = new NamedThreadFactory("Force Garbage collector")
					.setDaemon(true).setPriority(Thread.MIN_PRIORITY)
					.newThread(forceGc);
			gcThread.start();
		}
	}
}

