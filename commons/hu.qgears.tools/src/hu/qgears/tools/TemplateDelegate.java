package hu.qgears.tools;

import hu.qgears.rtemplate.runtime.Consumer;
import hu.qgears.rtemplate.runtime.DeferredTemplate;
import hu.qgears.rtemplate.runtime.DummyCodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public class TemplateDelegate extends RAbstractTemplatePart {

	public TemplateDelegate() {
		super(new DummyCodeGeneratorContext());
	}

	public void writeDelegate(String s) {
		write(s);
	}
	/**
	 * Add a method for deferred execution.
	 * The method will be called after all other code generation is done but its output will be
	 * inserted to the current offset. See {@link DeferredTemplate}.
	 * 
	 * The parameters are passed to the deferred method as an array of objects.
	 * 
	 * @param f
	 * @param param
	 * @return
	 */
	public DeferredTemplate deferredDelegate(final Consumer<Object[]> f, final Object ... param)
	{
		return super.deferred(f, param);
	}
	public String getResult()
	{
		templateState.flush();
		finishDeferredParts();
		String o=templateState.getOut().toString();
		return o;
	}

	public void finishDeferredDelegate() {
		templateState.flush();
		finishDeferredParts();
	}
}
