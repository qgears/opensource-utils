package hu.qgears.shm.jmalloc;

import java.lang.ref.WeakReference;

/**
 * Allocated entries are stored by this class in the hosting {@link JMalloc} instance.
 * Only stores a weak reference to the actual allocated entry thus
 * the entries are possible to be freed by the finalizer.
 */
public class JMallocMemoryEntry extends WeakReference<JMallocMemory>
{
	public final int start;
	public final int end;
	public JMallocMemoryEntry(JMallocMemory mem, int start, int end) {
		super(mem);
		this.start=start;
		this.end=end;
	}
}
