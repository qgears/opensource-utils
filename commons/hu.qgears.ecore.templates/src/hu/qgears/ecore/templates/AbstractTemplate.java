package hu.qgears.ecore.templates;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTemplate {

	private class Deferred {
		Runnable code;
		int position;
		
		public Deferred(Runnable code, int position) {
			super();
			this.code = code;
			this.position = position;
		}
		
		
	}
	
	protected SmartWriter rtout;
	protected SmartWriter rtcout;
	private List<Deferred> deferred = new ArrayList<>();
	
	protected class SmartWriter {
		
		private StringWriter real = new StringWriter();
		
		public void write (String content){
			real.write(content);
		}
		public void write (int number){
			real.write(String.valueOf(number));
		}
		
		public StringBuffer getBuffer() {
			return real.getBuffer();
		}
		
		@Override
		public String toString() {
			return real.toString();
		}
		public void writeClass(Class<?> clz) {
			real.write(addImport(clz));
		}
	}
	
	public AbstractTemplate() {
		rtcout = new SmartWriter();
		rtout = rtcout;
	}
	
	public String generate() {
		doGenerate();
		SmartWriter old = rtcout;
		int offset = 0;
		for (Deferred d : deferred) {
			SmartWriter temp = new SmartWriter();
			rtcout = rtout = temp;
			d.code.run();
			old.getBuffer().insert(offset+d.position, temp.getBuffer());
			offset+= temp.getBuffer().length();
		}
		rtcout = rtout = old;
		return rtcout.toString();
	}
	
	
	abstract protected void doGenerate();
	abstract String addImport(Class<?> clz);
	
	protected void deferred( Runnable r) {
		int current = rtout.getBuffer().length();
		if (deferred.isEmpty() || deferred.get(deferred.size()-1).position <= current) {
			deferred.add(new Deferred(r,current));
		} else {
			throw new RuntimeException("Cannot add deferred template BEFORE another registered tempalte.");
		}
	}
}
