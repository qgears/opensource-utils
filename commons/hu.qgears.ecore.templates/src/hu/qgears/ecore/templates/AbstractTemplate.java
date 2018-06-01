package hu.qgears.ecore.templates;

import java.io.StringWriter;

public abstract class AbstractTemplate {

	protected SmartWriter rtout;
	protected SmartWriter rtcout;

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
		return rtcout.toString();
	}
	
	
	abstract protected void doGenerate();
	abstract String addImport(Class<?> clz);
}
