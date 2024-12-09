package hu.qgears.commons;

import java.io.StringWriter;

/**
 * Abstract base for rTemplate template files.
 * @author rizsi
 *
 */
public abstract class AbstractTemplate implements ITemplate {
	protected StringWriter out, rtout, rtcout;

	public AbstractTemplate() {
		out = new StringWriter();
		rtout = out;
		rtcout = out;
	}
	public final String generate() throws Exception
	{
		doGenerate();
		return out.toString();
	}
	abstract  protected void doGenerate() throws Exception;
	@Override
	public StringWriter getWriter() {
		return out;
	}
}
