package hu.qgears.tools.build.compiler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import hu.qgears.commons.UtilString;

/**
 * Handles different ways of loading Java compiler API implementation.
 * In case JDK is used then simple ToolProvider.getSystemJavaCompiler() is used.
 * In case of JRE is used then environment variables must be configured to
 * load compiler class by name.
 */
public class CompilerLoader {
	public static final String compilerClass = System.getenv("COMPILER_CLASS");
	public static final String compilerClassCreator = System.getenv("COMPILER_CLASS_CREATOR")==null?"new":System.getenv("COMPILER_CLASS_CREATOR");
	public static final String compilerClassPath = System.getenv("COMPILER_CLASSPATH");
	private Supplier<JavaCompiler> compilerSupplier;
	private static CompilerLoader instance;
	public static JavaCompiler createCompilerInstance()
	{
		synchronized (CompilerLoader.class) {
			if(instance==null)
			{
				instance=new CompilerLoader();
			}
			instance.initialize();
		}
		if(instance.compilerSupplier==null)
		{
			throw new RuntimeException("Java Compiler can not be instantiated. Use JDK or configure COMPILER_CLASS, COMPILER_CLASS_CREATOR (='new') and COMPILER_CLASSPATH variables!");
		}
		return instance.compilerSupplier.get();
	}
	private void initialize() {
		if(compilerClass!=null&&compilerClass.length()>0)
		{
			try {
				ClassLoader cl=getClass().getClassLoader();
				// Example compiler classes:
				// "com.sun.tools.javac.api.JavacTool"
				// "org.eclipse.jdt.internal.compiler.tool.EclipseCompiler"
				if(compilerClassPath!=null && compilerClassPath.length()>0)
				{
					List<String> parts=UtilString.split(compilerClassPath, File.pathSeparator);
					List<URL> urls=new ArrayList<>();
					for(String s: parts)
					{
						File f=new File(s);
						URL u=f.toURI().toURL();
						urls.add(u);
						System.out.println("Override compiler classpath: "+compilerClassPath+" "+f.toURI().toURL());
					}
					cl=new URLClassLoader(urls.toArray(new URL[] {}), getClass().getClassLoader());
				}
				Class<?> c=cl.loadClass(compilerClass);
				compilerSupplier=()->{
					try {
						Object o;
						if("new".equals(compilerClassCreator))
						{
							o=c.getConstructor().newInstance();
						}else
						{
							Method m = c.getMethod(compilerClassCreator);
							o=m.invoke(null);
						}
						return (JavaCompiler) o;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					};
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}else
		{
			compilerSupplier=()->ToolProvider.getSystemJavaCompiler();
		}
	}
	public static void workaroundECJIssue(JavaCompiler compiler, StandardJavaFileManager standardFileManager) {
		if("EclipseCompiler".equals(compiler.getClass().getSimpleName()))
		{
			try {
				String javaHomePath=System.getenv("JAVA_HOME");
				System.out.println("JAVA_HOME="+javaHomePath);
				  // This hack is only needed in a Google-internal Java 8 environment where symbolic links make
				  // it hard for ecj to find the boot class path. Elsewhere it is unnecessary but harmless.
				  File rtJar = new File(javaHomePath + "/lib/rt.jar");
				  if (rtJar.exists()) {
				    List<File> bootClassPath = new ArrayList<>();
				        bootClassPath.add(rtJar);
				        Iterable<? extends File> fs=standardFileManager.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
				        if(fs!=null)
				        {
					        for(File f: fs)
					        {
					        	bootClassPath.add(f);
					        }
				        }
				    standardFileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootClassPath);
				  }else
				  {
					  System.err.println("rt.jar not found: "+rtJar);
				  }
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
