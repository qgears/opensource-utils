package hu.qgears.commons;

/**
 * Utility and convenience methods for the Java Runtime Environment.
 *  
 * @author chreex
 */
public class UtilJre {
	private UtilJre() {
		// Preventing instantiation
	}

	/**
	 * Extracts the Java 'feature' version, which is: 
	 * <ul>
	 * <li>the JRE minor version if the JRE version starts with "1.", which 
	 * will be true for JRE versions until 1.8.*
	 * <li>the JRE major version, if the JRE major version is larger than or
	 * equal to 9 
	 * </ul>
	 * @throws IllegalStateException if JRE version is totally unexpected and
	 * the feature version cannot be extracted from it 
	 * @return the Java 'feature' version
	 */
	public static int getJavaFeatureVersion() {
	    final String sysPropJavaVersion = System.getProperty("java.version");
	    final String versionStr;
	    
	    if (sysPropJavaVersion.startsWith("1.")) {
	        versionStr = sysPropJavaVersion.substring(2, 3);
	    } else {
	        final int dotIdx = sysPropJavaVersion.indexOf(".");
	        
	        if (dotIdx != -1) { 
	        	versionStr = sysPropJavaVersion.substring(0, dotIdx); 
	    	} else {
	    		throw new IllegalStateException("Unexpected 'java.version' "
	    				+ "system property value");
	    	}
	    }
	    
	    return Integer.parseInt(versionStr);
	}
	
	
}
