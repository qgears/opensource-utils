package hu.qgears.opengl.commons;

import java.io.StringWriter;
import java.lang.reflect.Field;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public class QueryCaps extends AbstractOpenglApplication2 {
	public static void main(String[] args) {
		try {
			new QueryCaps().execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void logic() {
		ContextCapabilities cc=GLContext.getCapabilities();
		Field[] fs=ContextCapabilities.class.getFields();
		StringWriter sw=new StringWriter();
//		File file=new File("opengl-caps.txt");
		for(Field f:fs)
		{
			try {
				Object value=f.get(cc);
				sw.write(f.getName()+" : "+value);
				sw.write("\n");
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(sw.toString());
		exit();
	}

	@Override
	protected void render() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected boolean isDirty() {
		return true;
	}
	@Override
	protected void logError(String message, Exception e) {
		e.printStackTrace();
	}

}
