package hu.qgears.emfcollab.load;

import org.eclipse.emf.ecore.EObject;

/**
 * Visitor design pattern for EMF models.
 * @author rizsi
 *
 */
public class UtilVisitor {
	public interface Visitor
	{
		Object visit(EObject element);
	}
	/**
	 * Visits the whole model sub tree of the elmement recursive.
	 * @param element
	 * @param visitor
	 * @return the return value of the first visitor returning different from null.
	 */
	public static Object visitModel(EObject element, Visitor visitor)
	{
		if(element==null)
		{
			return null;
		}
		Object ret=null;
		ret=visitor.visit(element);
		for(EObject obj:element.eContents())
		{
			Object subret=null;
			subret=visitModel(obj, visitor);
			if(ret==null)
			{
				ret=subret;
			}
		}
		return ret;
	}
	/**
	 * Visits the whole model sub-tree of the root element recursive.
	 * If the visitor of an element returns non-null then that element's children
	 * are not visited.
	 * @param element
	 * @param visitor
	 * @return the return value of the first visitor returning different from null.
	 */
	public static Object visitModelWithFinish(EObject element, Visitor visitor)
	{
		Object ret=null;
		ret=visitor.visit(element);
		if(ret==null)
		{
			for(EObject obj:element.eContents())
			{
				Object subret=null;
				subret=visitModel(obj, visitor);
				if(ret==null)
				{
					ret=subret;
				}
			}
		}
		return ret;
	}
}
