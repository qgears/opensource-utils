package hu.qgears.nativeloader;

/**
 * Native libraries that must be present on the preload path.
 */
public class NativePreload {
	public final String fileName;
	public final String resource;
	public NativePreload(String fileName, String resource) {
		this.fileName=fileName;
		this.resource=resource;
		if(fileName==null)
		{
			throw new NullPointerException("fileName must not be null");
		}
		if(resource==null)
		{
			throw new NullPointerException("resource must not be null");
		}
	}
}
