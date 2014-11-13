package hu.qgears.nativeloader;

public class NativeBinary {
	private String libPath;
	private String installPath;
	public NativeBinary(String libPath, String installPath) {
		super();
		this.libPath = libPath;
		this.installPath = installPath;
	}
	public String getLibPath() {
		return libPath;
	}
	public String getInstallPath() {
		return installPath;
	}
}
