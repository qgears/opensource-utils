package hu.qgears.shm.sem;

class SemaphoreNative {
	private long ptr;
	protected native void init(String id, int createType);
	protected native void nativeDispose(boolean delete);
	protected native int getValue();
	protected native void incrementValue();
	protected native void decrementValue();
	protected native boolean decrementValueTry();
	/**
	 * Windows and Linux API is different.
	 * 
	 * @param relativeTimeoutMillis - used by windows
	 * @param absoluteTimeoutTimeMillis - used by Linux
	 * @return
	 */
	protected native boolean decrementValueTimed(long relativeTimeoutMillis, long absoluteTimeoutTimeMillis);
}
