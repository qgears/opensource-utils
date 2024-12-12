package hu.qgears.shm.jmalloc;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import hu.qgears.commons.MultiMapTreeImpl;
import hu.qgears.commons.mem.INativeMemoryAllocator;
import hu.qgears.shm.UtilSharedMemory;
import hu.qgears.shm.part.PartNativeMemory;

/**
 * An allocator implementation that uses a single pool to allocate chunks from it.
 * Management structures are stored in Java objects only the target memory is stored
 * in unmanaged areas.
 * 
 * Implementation is best fit: returns the smallest partition that fits the query.
 */
public class JMalloc implements INativeMemoryAllocator {
	/**
	 * The allocator handles multiple pools.
	 */
	private TreeMap<Integer, JMallocPool> poolByAddress=new TreeMap<>();
	private JMallocPoolAllocator poolAllocator;
	private int currentPoolAddress=0;
	private int defaultAlignment=16;
	private int poolSize=5000000;
	private MultiMapTreeImpl<Integer, Unallocated> bySize=new MultiMapTreeImpl<>();
	private HashMap<Integer, Unallocated> byStart=new HashMap<>();
	private HashMap<Integer, Unallocated> byEnd=new HashMap<>();
	private TreeMap<Integer, JMallocMemoryEntry> allocatedBuffers=new TreeMap<>();
	private int allAllocated;
	/**
	 * Create a JMalloc native memory allocator.
	 * @param poolAllocator This allocator allocates pools from which the memory pieces are allocated. This will be called on demand to create noew pools.
	 * @param poolSize This is the size of each pool
	 * @param defaultAlignment this is the alignment of returned chunks.
	 */
	public JMalloc(JMallocPoolAllocator poolAllocator, int poolSize, int defaultAlignment) {
		super();
		// Load native library - clearMemory is the method that is used from here.
		UtilSharedMemory.getInstance();
		this.poolAllocator = poolAllocator;
		this.poolSize = poolSize;
		this.defaultAlignment = defaultAlignment;
		if(poolSize%defaultAlignment!=0)
		{
			throw new IllegalArgumentException("Pool size/alignment must be a whole number.");
		}
	}
	@Override
	public JMallocMemory allocateNativeMemory(long sizeLong, int align) {
		if(sizeLong>Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("Size more than "+Integer.MAX_VALUE+" is not implemented.");
		}
		if(sizeLong<1)
		{
			throw new IllegalArgumentException("Size less than 1 is invalid.");
		}
		if(sizeLong>poolSize)
		{
			throw new IllegalArgumentException("Size more than pool size is invalid.("+sizeLong+">"+poolSize+")");
		}
		int size=(int)sizeLong;
		if(align>defaultAlignment)
		{
			throw new IllegalArgumentException("Alignment more than "+defaultAlignment+" is not implemented.");
		}
		JMallocMemory ret=null;
		do
		{
			synchronized (poolByAddress) {
				Integer bestFitSize=bySize.ceilingKey(size);
				if(bestFitSize!=null)
				{
					List<Unallocated> unallocateds=bySize.get(bestFitSize);
					if(unallocateds.size()==0)
					{
						throw new OutOfMemoryError("Internal error in JMalloc. Best fit size: "+bestFitSize);
					}
					Unallocated toAllocateFrom=unallocateds.remove(unallocateds.size()-1);
					if(unallocateds.size()==0)
					{
						bySize.remove(bestFitSize);
					}
					byStart.remove(toAllocateFrom.start);
					byEnd.remove(toAllocateFrom.end);
					
					int sizeAligned=((size+defaultAlignment-1)/defaultAlignment)*defaultAlignment;
					
					int remainingSize=toAllocateFrom.size-sizeAligned;
					if(remainingSize>0)
					{
						Unallocated remaining=new Unallocated(toAllocateFrom.pool, toAllocateFrom.size-sizeAligned, toAllocateFrom.start+sizeAligned, toAllocateFrom.end);
						bySize.putSingle(remaining.size, remaining);
						byStart.put(remaining.start, remaining);
						byEnd.put(remaining.end, remaining);
					}
					ByteBuffer bb=toAllocateFrom.pool.getJavaAccessor();
					int start=toAllocateFrom.start-toAllocateFrom.pool.poolOffset;
					bb.limit(start+size);
					bb.position(start);
					ByteBuffer allocated=bb.slice();
					ret=new JMallocMemory(this, toAllocateFrom.pool, allocated, size, align,
							toAllocateFrom.start, toAllocateFrom.start, toAllocateFrom.start+sizeAligned);
					allocatedBuffers.put(ret.start, new JMallocMemoryEntry(ret, ret.start, ret.end));
					allAllocated+=sizeAligned;
				}
			}
			if(ret==null)
			{
				// Allocate a new pool when there is not enough memory to allocate chunk.
				// The allocator may throw OOM
				JMallocPool poolCreated=poolAllocator.allocateNewPool(poolSize);
				if(poolCreated.nmem.getJavaAccessor().capacity()!=poolSize)
				{
					throw new OutOfMemoryError("Illegal result of new pool allocation.");
				}
				synchronized (poolByAddress) {
					Unallocated unallocated=new Unallocated(poolCreated, poolSize, currentPoolAddress, currentPoolAddress+poolSize);
					poolCreated.poolOffset=currentPoolAddress;
					poolByAddress.put(currentPoolAddress, poolCreated);
					currentPoolAddress+=poolSize;
					currentPoolAddress+=defaultAlignment;
					bySize.putSingle(unallocated.size, unallocated);
					byStart.put(unallocated.start, unallocated);
					byEnd.put(unallocated.end, unallocated);
				}
			}
		} while (ret==null);
		PartNativeMemory.clearBuffer(ret.getJavaAccessor());
		return ret;
	}
	/**
	 * Put the memory chunk back to the registry.
	 * @param m
	 * @param bb
	 */
	protected void free(JMallocMemory m, ByteBuffer bb)
	{
		synchronized (poolByAddress) {
			int returnedSize=m.end-m.start;
			int start=m.start;
			int end=m.end;
			Unallocated after=byStart.remove(m.end);
			if(after!=null)
			{
				byEnd.remove(after.end);
				bySize.removeSingle(after.size, after);
				returnedSize+=after.size;
				end=after.end;
			}
			Unallocated before=byEnd.remove(m.start);
			if(before!=null)
			{
				byStart.remove(before.start);
				bySize.removeSingle(before.size, before);
				returnedSize+=before.size;
				start=before.start;
			}
			Unallocated returned=new Unallocated(m.pool, returnedSize, start, end);
			byEnd.put(returned.end, returned);
			byStart.put(returned.start, returned);
			bySize.putSingle(returned.size, returned);
			allocatedBuffers.remove(m.start);
			allAllocated-=m.end-m.start;
		}
	}

	@Override
	public JMallocMemory allocateNativeMemory(long size) {
		return allocateNativeMemory(size, defaultAlignment);
	}

	@Override
	public int getDefaultAlignment() {
		return defaultAlignment;
	}

	public boolean isDisposed() {
		return false;
	}
	public int getPoolSize() {
		return poolSize;
	}
	public int getNPool()
	{
		return poolByAddress.size();
	}
	/**
	 * Check the integrity of the catalog.
	 */
	public void selfCheck()
	{
		synchronized (poolByAddress) {
			Set<Unallocated> all=new HashSet<>();
			Set<JMallocMemoryEntry> allm=new HashSet<>();
			int at=0;
			int countAllocated=0;
			for(Map.Entry<Integer, JMallocPool> poolEntry: poolByAddress.entrySet())
			{
				boolean prevUnallocated=false;
				int start=poolEntry.getKey();
				int end=start+poolSize;
				myassert(at==start);
				while(at<end)
				{
					myassert(at%defaultAlignment == 0);
					JMallocMemoryEntry wref=allocatedBuffers.get(at);
					Unallocated ua=byStart.get(at);
					myassert(wref!=null || ua!=null);
					myassert(wref==null || ua==null);
					if(wref!=null)
					{
						JMallocMemory mem=wref.get();
						if(mem!=null)
						{
							JMallocPool p=mem.pool;
							myassert(mem.start-p.poolOffset==PartNativeMemory.getOffset(p.nmem.getJavaAccessor(), mem.getJavaAccessor()));
							myassert(mem.start==at);
							myassert(mem.end<=end);
							myassert(mem.end>mem.start);
							myassert(mem.getSize()<=mem.end-mem.start);
							myassert(mem.getSize()+defaultAlignment>mem.end-mem.start);
							myassert(mem.getSize()>0);
							myassert(mem.pool==poolEntry.getValue());
						}
						at=wref.end;
						allm.add(wref);
						prevUnallocated=false;
						countAllocated+=wref.end-wref.start;
					}
					if(ua!=null)
					{
						myassert(!prevUnallocated);
						prevUnallocated=true;
						all.add(ua);
						myassert(ua.start==at);
						myassert(ua.end<=end);
						myassert(ua.end>ua.start);
						at=ua.end;
						myassert(bySize.get(ua.size).contains(ua));
						myassert(byEnd.get(ua.end)==ua);
					}
				}
				at+=defaultAlignment;
			}
			for(Integer i: byEnd.keySet())
			{
				myassert(byEnd.get(i).end==i);
				myassert(all.contains(byEnd.get(i)));
			}
			for(Integer i: byStart.keySet())
			{
				myassert(byStart.get(i).start==i);
				myassert(all.contains(byStart.get(i)));
			}
			for(int s: bySize.keySet())
			{
				myassert(bySize.get(s).size()>0);
				for(Unallocated u: bySize.get(s))
				{
					myassert(u.size==s);
					myassert(u.size==u.end-u.start);
					myassert(all.contains(u));
				}
			}
			for(int addr: allocatedBuffers.keySet())
			{
				JMallocMemoryEntry wr=allocatedBuffers.get(addr);
				myassert(allm.contains(wr));
				myassert(wr.start==addr);
			}
			myassert(countAllocated==allAllocated);
		}
	}
	private void myassert(boolean b) {
		if(!b)
		{
			throw new RuntimeException();
		}
	}
	public int getAllAllocated() {
		return allAllocated;
	}
}
