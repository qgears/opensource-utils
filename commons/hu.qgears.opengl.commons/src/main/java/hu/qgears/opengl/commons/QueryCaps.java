package hu.qgears.opengl.commons;

import java.io.StringWriter;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import hu.qgears.opengl.osmesa.Log4Init;


public class QueryCaps extends AbstractOpenglApplication2 {
	
	private static final Logger LOG = Logger.getLogger(QueryCaps.class);
	
	public static void main(String[] args) {
		try {
			Log4Init.init();
			new QueryCaps().execute();
		} catch (Exception e) {
			LOG.error("Error executing QueryCaps",e);
		}
	}
	@Override
	protected void logic() {
		GLCapabilities cc = GL.getCapabilities();
		Field[] fs=GLCapabilities.class.getFields();
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
