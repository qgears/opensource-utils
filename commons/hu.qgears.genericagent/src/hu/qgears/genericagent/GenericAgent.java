package hu.qgears.genericagent;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;

public class GenericAgent {
	public static void agentmain(String agentArgs, Instrumentation inst) {
		try {
			String[] urlsS=agentArgs.split(",");
			URL urls[]=new URL[urlsS.length-2];
			for(int i=0;i<urlsS.length-2;++i)
			{
				urls[i]=new URL(urlsS[i]);
			}
			@SuppressWarnings("resource")
			ClassLoader cl = new URLClassLoader(urls);
			Class<?> c=cl.loadClass(urlsS[urlsS.length-2]);
			String parameter=urlsS[urlsS.length-1];
			Object o=c.newInstance();
			c.getMethod("run", String.class).invoke(o, parameter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
