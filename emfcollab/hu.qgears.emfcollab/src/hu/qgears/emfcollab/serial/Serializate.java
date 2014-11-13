package hu.qgears.emfcollab.serial;

import hu.qgears.commons.UtilString;
import hu.qgears.emfcollab.EmfEvent;
import hu.qgears.emfcollab.impl.EmfSession;
import hu.qgears.emfcollab.srv.EmfCommand;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


public class Serializate {
	Writer out, rtout, rtcout;
	public Serializate(Writer wr) {
		out=rtout=rtcout=wr;
		skipped.add("serialVersionUID");
	}
	Set<String> skipped=new HashSet<String>();
//	public void serializate(List<EmfCommand> commands) throws IllegalArgumentException, IllegalAccessException, IOException
//	{
//		for(EmfCommand c: commands)
//		{
//			serializate("EXECUTE",c);
//		}
//	}
	public void serializate(String type, EmfCommand c) throws IllegalArgumentException, IllegalAccessException, IOException
	{
		timestamp();
		out.write(""+type+" ");
		writeObject(c.getOwner().getUserName());
		out.write(" ");
		writeObject(c.getName());
		out.write(" ");
		writeObject(c.getCommandIndex());
		out.write(" ");
		writeObject(c.getOwner().getId());
		out.write("\n");
		for(EmfEvent e: c.getEvents())
		{
			serializate(e);
		}
	}
	private void timestamp() throws IOException {
		out.write(UtilString.padLeft(""+System.currentTimeMillis(), 14, '0'));
		out.write(' ');
	}
	public void serializate(EmfEvent event) throws IllegalArgumentException, IllegalAccessException, IOException
	{
		timestamp();
		out.write("EVENT "+event.getType());
		out.write(" ");
		for(Field f:event.getClass().getDeclaredFields())
		{
			String name=f.getName();
			if(skipped.contains(name))
			{
				continue;
			}
			f.setAccessible(true);
			Object o=f.get(event);
			out.write(""+name+": ");
			writeObject(o);
			out.write(" ");
		}		
		out.write("\n");
	}
	private void writeObject(Object o) throws IOException {
		if(o==null)
		{
			out.write("N");
			return;
		}
		if(o instanceof Long)
		{
			out.write("L"+o);
			return;
		}
		if(o instanceof Integer)
		{
			out.write("I"+o);
			return;
		}if(o instanceof String)
		{
			out.write("S\"");
			String s=(String)o;
			for(int i=0;i<s.length();++i)
			{
				char c=s.charAt(i);
				if((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9'))
				{
					out.write(c);
				}else if (c=='\n')
				{
					out.write("\\n");
				}else if (c=='\r')
				{
					out.write("\\r");
				}else
				{
					out.write("\\u");
					out.write(UtilString.padLeft(Integer.toHexString((int)c), 4, '0'));
				}
			}
			out.write("\"");
		}
	}
	public void flush() throws IOException {
		out.flush();
	}
	public void dispose() {
		try {
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void log(EmfSession session, String string) {
		try {
			timestamp();
			out.write(" "+string+" ");
			writeObject(session.getUserName());
			out.write(" ");
			writeObject(session.getSessionId());
			out.write("\n");
			flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void log(String string) {
		try {
			timestamp();
			out.write(" "+string+" ");
			out.write("\n");
			flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
