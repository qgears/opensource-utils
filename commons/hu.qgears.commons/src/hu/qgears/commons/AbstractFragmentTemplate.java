package hu.qgears.commons;

import java.io.StringWriter;

/**
 * Abstract base for rTemplate template files.
 * @author rizsi
 *
 */
abstract public class AbstractFragmentTemplate implements ITemplate {
	protected StringWriter out, rtout, rtcout;
	public AbstractFragmentTemplate(ITemplate parent) {
		out=rtout=rtcout=parent.getWriter();
	}
	public AbstractFragmentTemplate() {
	}
	public AbstractFragmentTemplate setParent(ITemplate parent)
	{
		out=rtout=rtcout=parent.getWriter();
		return this;
	}
	final public String generate()
	{
		doGenerate();
		return out.toString();
	}
	abstract  protected void doGenerate();
	@Override
	public StringWriter getWriter() {
		return out;
	}
}
