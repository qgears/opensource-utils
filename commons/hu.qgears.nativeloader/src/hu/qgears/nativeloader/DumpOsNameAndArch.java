package hu.qgears.nativeloader;

/**
 * Console application to dump the os.arch and os.name 
 * strings of your platform 
 * @author rizsi
 *
 */
public class DumpOsNameAndArch {
	public static void main(String[] args) {
		String archKey = "os.arch";
		String oskey = "os.name";
		String arch = System.getProperty(archKey);
		String os = System.getProperty(oskey);
		String key = os + "." + arch;
		System.out.println("OS   :'"+os+"'");
		System.out.println("ARCH :"+arch);
		System.out.println("KEY  :"+key);
	}
}
