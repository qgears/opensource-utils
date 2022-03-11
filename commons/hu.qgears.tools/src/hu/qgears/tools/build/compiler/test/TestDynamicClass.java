package hu.qgears.tools.build.compiler.test;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

import hu.qgears.commons.UtilFile;
import hu.qgears.tools.build.compiler.CompilerVersionConfiguration;
import hu.qgears.tools.build.compiler.DynamicCompiler;
import hu.qgears.tools.build.compiler.ICompileCallback;
import hu.qgears.tools.build.compiler.InMemoryForwardingFileManager;

public class TestDynamicClass {
	InMemoryForwardingFileManager imfm;
	public void test01() throws Exception
	{
		ClassLoader classLoader=getClass().getClassLoader();
		Class<? extends MyInterface> cla=DynamicCompiler.compileDynamic(classLoader,
				"hu.qgears.tools.build.compiler.test.Example", UtilFile.loadAsString(getClass().getResource("Example.txt")),
				MyInterface.class, 
				new ICompileCallback() {
			@Override
			public void getFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
				System.out.println("Compiled class in memory: "+className);
			}
		}, new CompilerVersionConfiguration().setSource("1.8"));
	    Object inst = cla.getDeclaredConstructor().newInstance();
	    MyInterface r=(MyInterface) inst;
	    r.myCall();
	}
	public static void main(String[] args) throws Exception {
		new TestDynamicClass().test01();
	}
}
