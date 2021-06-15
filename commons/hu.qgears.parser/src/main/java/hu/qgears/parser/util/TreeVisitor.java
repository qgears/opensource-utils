package hu.qgears.parser.util;

import java.util.List;

import hu.qgears.parser.ITreeElem;

public abstract class TreeVisitor {
	final public void visit(ITreeElem tree) throws Exception
	{
		visit(tree, 0);
	}
	final public void visit(ITreeElem tree, int depth) throws Exception
	{
		AutoCloseable c=visitNode(tree, depth);
		if(c!=null)
		{
			List<? extends ITreeElem> subs=tree.getSubs();
			for(ITreeElem sub:subs)
			{
				visit(sub, depth+1);
			}
			c.close();
		}
	}

	abstract protected AutoCloseable visitNode(ITreeElem tree, int depth) throws Exception;
}
