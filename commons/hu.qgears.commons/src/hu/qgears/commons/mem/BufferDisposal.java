package hu.qgears.commons.mem;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import hu.qgears.commons.AbstractReferenceCountedDisposeable;

/**
 * Provides unified interface for 'direct byte buffer' disposal, choosing the
 * right implementation autodetected by {@link #programmaticDispose system 
 * property} and {@link #getJavaFeatureVersion() JRE version}. 
 * 
 * @see #programmaticDispose
 * @author chreex
 */
public class BufferDisposal {
	
	/**
	 * Boolean system property for enabling programmatic disposal of allocated
	 * direct buffer memory: 
	 * {@literal "hu.qgears.commons.mem.programmaticDispose"}.
	 * <br>
	 * <ul>
	 * <li>If {@code true}, {@link AbstractReferenceCountedDisposeable#dispose()}
	 * will dispose allocated native memory. Note that onwards, accessing the
	 * disposed memory must not be attempted.
	 * <li>If {@code false}, {@link AbstractReferenceCountedDisposeable#dispose()}
	 * will have no effect. In this case, the JVM will free the allocated
	 * memory, according to its default behavior.
	 * </ul> 
	 */
	private static final boolean programmaticDispose =
			Boolean.getBoolean("hu.qgears.commons.mem.programmaticDispose");

	public interface Cleaner {
		public void clean(final ByteBuffer buffer) 
        		throws ReflectiveOperationException;
	}
	
	private static final Cleaner CLEANER_INSTANCE;

	/**
	 * Extracts the Java 'feature' version, which is: 
	 * <ul>
	 * <li>the JRE minor version if the JRE version starts with "1.", which 
	 * will be true for JRE versions until 1.8.*
	 * <li>the JRE major version, if the JRE major version is larger than or
	 * equal to 9 
	 * </ul>
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

	private static final class Java8Cleaner implements Cleaner {

		private final Method cleanerMethod;
		private final Method cleanMethod;

		private Java8Cleaner() throws ReflectiveOperationException, SecurityException {
			cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean");
			cleanerMethod = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
		}

		public void clean(final ByteBuffer buffer) 
				throws ReflectiveOperationException {
			final Object cleaner = cleanerMethod.invoke(buffer);
			if (cleaner != null) {
				cleanMethod.invoke(cleaner);
			}
		}
	}

    private static final class Java9Cleaner implements Cleaner {

        private final Object theUnsafe;
        private final Method invokeCleaner;

        private Java9Cleaner() throws ReflectiveOperationException, SecurityException {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            theUnsafe = field.get(null);
            invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
        }

        @Override
        public void clean(final ByteBuffer buffer) 
        		throws ReflectiveOperationException {
            invokeCleaner.invoke(theUnsafe, buffer);
        }
    }
    
    /**
     * Lets the JVM perform the cleanup. 
     *  
     * @author chreex
     */
    private static final class NoopCleaner implements Cleaner {
    	@Override
    	public void clean(final ByteBuffer buffer) throws ReflectiveOperationException {
    		// NOOP
    	}
    }
    
    static {
		if (programmaticDispose) {
			final int javaFeatureVersion = getJavaFeatureVersion();
			Cleaner tmpCleanerInstance = null;
			
			try {
				if (javaFeatureVersion <= 8) {
					tmpCleanerInstance = new Java8Cleaner();
				} else {
					tmpCleanerInstance = new Java9Cleaner();
				}
			} catch (final Exception e) {
				throw new IllegalStateException(e);
			}
			CLEANER_INSTANCE = tmpCleanerInstance;
		} else {
			CLEANER_INSTANCE = new NoopCleaner();
		}
    }

	public static Cleaner getCleanerInstance() {
		return CLEANER_INSTANCE;
	}

}
