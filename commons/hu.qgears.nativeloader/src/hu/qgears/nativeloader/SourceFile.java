package hu.qgears.nativeloader;

public class SourceFile {
	private String path;
	private String exec;
	private String out;
	private String installPath;
	public SourceFile(String path, String exec, String out, String installPath) {
		super();
		this.path = path;
		this.exec = exec;
		this.out = out;
		this.installPath=installPath;
	}
	public String getPath() {
		return path;
	}
	public String getExec() {
		return exec;
	}
	public String getOut() {
		return out;
	}
	public String getInstallPath() {
		return installPath;
	}
}
