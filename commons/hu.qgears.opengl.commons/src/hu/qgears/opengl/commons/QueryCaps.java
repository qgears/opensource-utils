package hu.qgears.opengl.commons;

import java.io.StringWriter;
import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;


public class QueryCaps extends AbstractOpenglApplication2 {
	
	private static final Logger LOG = LogManager.getLogger(QueryCaps.class);
	
	public static void main(String[] args) {
		try {
			new QueryCaps().execute();
		} catch (Exception e) {
			LOG.error("Error executing QueryCaps",e);
		}
	}
	@Override
	protected void logic() {
		ContextCapabilities cc=GLContext.getCapabilities();
		Field[] fs=ContextCapabilities.class.getFields();
		StringWriter sw=new StringWriter();
		for(Field f:fs)
		{
			try {
				Object value=f.get(cc);
				sw.write(f.getName()+" : "+value);
				sw.write("\n");
			} catch (Exception e) {
				LOG.error("Error reading capabilities",e);
			}
		}
		LOG.info(sw.toString());
		exit();
	}

	@Override
	protected void render() {
		//nothing to do
	}
	@Override
	protected boolean isDirty() {
		return true;
	}
	@Override
	protected void logError(String message, Exception e) {
		LOG.error(message,e);
	}

}
