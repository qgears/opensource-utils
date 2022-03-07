package hu.qgears.parser.util;

import java.util.List;

import hu.qgears.parser.ITreeElem;

/**
 * Visitor pattern for a parsed AST.
 */
public abstract class TreeVisitor {
	/**
	 * Visit a tree using this visitor.
	 * @param tree
	 * @throws Exception
	 */
	final public void visit(ITreeElem tree) throws Exception
	{
		visit(tree, 0);
	}
	/**
	 * Visit a tree using this visitor. Also count the visit depth and start counting from the
	 * parameter.
	 * @param tree
	 * @param depth
	 * @throws Exception
	 */
	final public void visit(ITreeElem tree, int depth) throws Exception
	{
		AutoCloseable c=visitNode(tree, depth);
		if(c!=null)
		{
			try
			{
				List<? extends ITreeElem> subs=tree.getSubs();
				for(ITreeElem sub:subs)
				{
					visit(sub, depth+1);
				}
			}finally
			{
				c.close();
			}
		}
	}
	/**
	 * A node is visited. The method is called before children are iterated.
	 * @param tree
	 * @param depth
	 * @return null means no visit of children non null is executed when the subtree visit was finished.
	 * @throws Exception
	 */
	abstract protected AutoCloseable visitNode(ITreeElem tree, int depth) throws Exception;
}
