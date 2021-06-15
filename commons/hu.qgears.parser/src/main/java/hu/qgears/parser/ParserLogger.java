package hu.qgears.parser;

import java.io.PrintStream;

import hu.qgears.parser.impl.ElemBuffer;

/**
 * Logger that logs some data of the parsing.
 * 
 * @author rizsi
 * 
 */
public class ParserLogger {
	private PrintStream err;
	private long time;

	public ParserLogger(PrintStream err) {
		super();
		this.err = err;
	}
	public ParserLogger() {
		this(null);
	}

	public void logStart() {
		if(err!=null)
		{
			err.println("Start parsing");
		}
		time = System.nanoTime();
	}

	public void logTableFilled(ElemBuffer buffer, int size, int currentGroup, int tokensSize) {
		if(err!=null)
		{
			err.println("Table filled " + getElapsed() + "nanos " + size + " "
				+ currentGroup + " " + tokensSize + " "
				+ ((double) size / currentGroup)+" Nanos stored already search: "+buffer.getNanosStoredAlready()+" do generates: "+buffer.nanosDoGenerates);
		}
	}

	private long getElapsed() {
		long t = System.nanoTime();
		long ret = t - time;
		time = t;
		return ret;
	}

	public void logTokenized() {
		if(err!=null)
		{
			err.println("Tokenized " + getElapsed()+"nanos");
		}
	}

	public void logTreeBuild() {
		if(err!=null)
		{
			err.println("Tree build " + getElapsed()+"nanos");
		}
	}

	public void logTreeFiltered() {
		if(err!=null)
		{
			err.println("Tree filtered " + getElapsed()+"nanos");
		}
	}
	public void println(String print) {
		if(err!=null)
		{
			err.println(print);
		}
	}

}
