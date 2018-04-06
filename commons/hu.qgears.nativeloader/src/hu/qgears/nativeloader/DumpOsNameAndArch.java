package hu.qgears.nativeloader;

/**
 * Console application to dump the os.arch and os.name strings of your platform.
 * 
 * @author rizsi
 *
 */
public class DumpOsNameAndArch {
	
	public static void main(String[] args) {
		new DumpOsNameAndArch().run();
	}

	private void run() {
		String archKey = "os.arch";
		String oskey = "os.name";
		String osVersionKey = "os.version";
		String arch = System.getProperty(archKey);
		String os = System.getProperty(oskey);
		String osVersion = System.getProperty(osVersionKey);
		String key = os + "." + arch;
		/*
		 * This is a console application
		 */
		System.out.println("OS   :'"+os+"'");//NOSONAR
		System.out.println("ARCH :"+arch);//NOSONAR
		System.out.println("KEY  :"+key);//NOSONAR
		System.out.println("VER  :"+osVersion);//NOSONAR
	}
}
