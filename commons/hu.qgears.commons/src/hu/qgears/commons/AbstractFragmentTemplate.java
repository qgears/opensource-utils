package hu.qgears.commons;

import java.io.StringWriter;

/**
 * Abstract base for rTemplate template files.
 * @author rizsi
 *
 */
public abstract class AbstractFragmentTemplate implements ITemplate {
	protected StringWriter out, rtout, rtcout;

	public AbstractFragmentTemplate(ITemplate parent) {
		rtcout = parent.getWriter();
		out = rtcout;
		rtout = rtcout;
	}
	public AbstractFragmentTemplate() {
		//default constructor for subclasses
	}
	public AbstractFragmentTemplate setParent(ITemplate parent)
	{
		rtcout = parent.getWriter();
		out = rtcout;
		rtout = rtcout;
		return this;
	}
	public final String generate()
	{
		doGenerate();
		return out.toString();
	}
	protected abstract void doGenerate();
	@Override
	public StringWriter getWriter() {
		return out;
	}
}
