package hu.qgears.parser;

import java.io.PrintStream;

import hu.qgears.commons.UtilString;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.tokenizer.ITextSource;

/**
 * Logger that logs some data of the parsing.
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
			err.println("Table filled " + getElapsedString() + " size: " + size + " "
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
			err.println("Tokenized " + getElapsedString());
		}
	}

	public void logTreeBuild() {
		if(err!=null)
		{
			err.println("Tree build " + getElapsedString());
		}
	}

	public void logTreeFiltered() {
		if(err!=null)
		{
			err.println("Tree filtered " + getElapsedString());
		}
	}
	private String getElapsedString() {
		long t=getElapsed();
		long last=t%1000;
		t/=1000;
		long last2=t%1000;
		t/=1000;
		long last3=t%1000;
		// TODO Auto-generated method stub
		return ""+last3+ ","+UtilString.padLeft(""+last2, 3, '0')+"," +UtilString.padLeft(""+last, 3, '0')+" nanos";
	}
	/**
	 * State of the parse buffer when parsing stuck. Useful for the parser and grammar developer.
	 * Default implementation does nothing.
	 * {@link IParserReceiver} also receives the current buffer state
	 * @param buffer
	 * @param iTextSource 
	 */
	public void logStateWhenParseStuck(ElemBuffer buffer, ITextSource iTextSource) {
	}
	public void println(String print) {
		if(err!=null)
		{
			err.println(print);
		}
	}
	public void logTokenizedUnfiltered() {
		if(err!=null)
		{
			err.println("Tokenized unfiltered" + getElapsedString());
		}
	}

}
