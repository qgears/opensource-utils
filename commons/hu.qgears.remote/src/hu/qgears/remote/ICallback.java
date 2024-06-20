package hu.qgears.remote;

public interface ICallback {
	void close();

	void data(byte[] copyOf);

}
