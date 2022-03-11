package hu.qgears.tools.build.compiler;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

/**
 * Compile a single Java source file on the fly and load the binary.
 */
public class DynamicCompiler {
	/**
	 * Compile a Java class from source and load it into a {@link Class}
	 * How compiler is accessed: see {@link CompilerLoader}
	 * Does not create temporary files but stores everything in RAM.
	 * @param <T> Interface or superclass of class to be loaded
	 * @param classLoader class loader that can load all dependencies of the source file.
	 * @param className name of the class. Example format: hu.qgears.tools.Tools
	 * @param source Source of the class to be compiled
	 * @param interfac the loaded class is casted to this class or interface (may be Object if unknown)
	 * @param compilerCallback log compile diagnostics. null is allowed: logs to stderr
	 * @param config Configuration of the Java compiler versions. A newly created instance is enough.
	 * @return the compiled class loaded into a new {@link URLClassLoader} instance.
	 * @throws ClassNotFoundException
	 */
	public static <T> Class<? extends T> compileDynamic(ClassLoader classLoader, String className, String source, Class<T> interfac,
			ICompileCallback compilerCallback, CompilerVersionConfiguration config) throws ClassNotFoundException
	{
		InMemoryForwardingFileManager[] imfm=new InMemoryForwardingFileManager[1];
		List<JavaFileObject> compilationUnits=new ArrayList<>();
		JavaStringObject jso=new JavaStringObject(className, source);
		compilationUnits.add(jso);
		CompileCommandBuilder ccb=new CompileCommandBuilder() {
			@Override
			public JavaFileManager wrapFileManager(StandardJavaFileManager standardFileManager) {
				super.wrapFileManager(standardFileManager);
				return imfm[0]=new InMemoryForwardingFileManager(standardFileManager, classLoader, compilerCallback);
			}
			@Override
			public Iterable<? extends JavaFileObject> getCompilationUnits() {
				return compilationUnits;
			}
			@Override
			public File createGetOutputFolder() {
				return null;
			}
		};
		ccb.compilerCallback=compilerCallback;
		ccb.copyCompilerVersionFrom(config);
		new ExecuteCompileCommand().exec(ccb);
	    ClassLoader cl = imfm[0].createCompiledClassLoader();
	    Class<?> cla0 = cl.loadClass(className);
	    Class<? extends T> cla = cla0.asSubclass(interfac);
	    return cla;
	}
}
