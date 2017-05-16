package hu.qgears.tools;

import java.util.List;

import joptsimple.annot.AnnotatedClass;

abstract public class AbstractTool implements ITool
{
	public interface IArgs
	{
		void validate();
	}
	@Override
	final public int exec(List<String> subList) throws Exception {
		IArgs a=createArgsObject();
		AnnotatedClass ac=new AnnotatedClass();
		ac.parseAnnotations(a);
		ac.parseArgs(subList.toArray(new String[]{}));
		try
		{
			a.validate();
		}catch(Exception e)
		{
			if(e instanceof IllegalArgumentException)
			{
				System.out.println("Illegal arguments: "+e.getMessage());
			}else
			{
				e.printStackTrace();
			}
			System.out.println(getId()+": "+getDescription());
			AnnotatedClass ac2=new AnnotatedClass();
			ac2.parseAnnotations(createArgsObject());
			ac2.printHelpOn(System.out);
			return 1;
		}
		return doExec(a);
	}
	@Override
	public int help(List<String> subList) throws Exception {
		System.out.println(getId()+": "+getDescription());
		System.out.println("\n\nARGUMENTS:");
		AnnotatedClass ac2=new AnnotatedClass();
		ac2.parseAnnotations(createArgsObject());
		ac2.printHelpOn(System.out);
		return 0;
	}
	abstract protected int doExec(IArgs a) throws Exception;
	abstract protected IArgs createArgsObject();
}
