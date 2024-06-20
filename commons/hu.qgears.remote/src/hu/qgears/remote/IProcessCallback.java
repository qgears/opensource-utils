package hu.qgears.remote;

public interface IProcessCallback extends ICallback {
	void destroy();
	public int exitValue(long timeoutMillis) throws InterruptedException;
}
