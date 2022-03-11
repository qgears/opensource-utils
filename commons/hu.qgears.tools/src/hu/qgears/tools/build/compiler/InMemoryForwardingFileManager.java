package hu.qgears.tools.build.compiler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class InMemoryForwardingFileManager extends ForwardingJavaFileManager<StandardJavaFileManager>{
	private ClassLoader classLoader;
	private Map<String, JavaByteObject> created=new HashMap<>();
	private ICompileCallback log;
	public InMemoryForwardingFileManager(StandardJavaFileManager fileManager, ClassLoader classLoader, ICompileCallback log) {
		super(fileManager);
		this.classLoader = classLoader;
		this.log=log;
	}
	@Override
	public ClassLoader getClassLoader(Location location) {
		return classLoader;
	}
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className, JavaFileObject.Kind kind,
                                               FileObject sibling) throws IOException {
    	try {
    		className=className.replaceAll("/", "\\.");
    		if(log!=null)
    		{
    			log.getFileForOutput(location, className, kind, sibling);
    		}
			JavaByteObject ret=new JavaByteObject(className);
			created.put(className, ret);
			return ret;
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
    }
    public ClassLoader createCompiledClassLoader()
    {
    	return new ClassLoader(classLoader) {
            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                //no need to search class path, we already have byte code.
                byte[] bytes = created.get(name).getBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
		};
    }
}
