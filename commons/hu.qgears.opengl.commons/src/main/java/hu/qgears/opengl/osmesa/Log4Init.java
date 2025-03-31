package hu.qgears.opengl.osmesa;

import java.util.Enumeration;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Log4Init {

	public static void init() {
		// add appender to any Logger (here is root)
		Enumeration<?> apps=Logger.getRootLogger().getAllAppenders();
		if(!apps.hasMoreElements())
		{
			System.err.println("Zero Log4j appenders. Log4j is configured to log to console by: "+Log4Init.class.getName());
			ConsoleAppender console = new ConsoleAppender(); // create appender
			// configure the appender
			String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			console.setThreshold(Level.ALL);
			console.activateOptions();
			Logger.getRootLogger().addAppender(console);
			// httpclient floods the output in many cases. Disable it:
			Logger.getLogger("httpclient.wire.content").setLevel(Level.INFO);
			Logger.getLogger("org.apache.http").setLevel(Level.INFO);
		}
	}

}
