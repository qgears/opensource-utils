package hu.qgears.emfcollab.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Visitor design pattern for EMF models.
 * Useful because this avoids resolving proxies when visiting all nodes of the model.
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
	public static Object visitModel(ResourceSet rs, Visitor visitor)
	{
		Object ret=null;
		for(Resource r: rs.getResources())
		{
			for(EObject eo: r.getContents())
			{
				Object subret=null;
				subret=visitModel(eo, visitor);
				if(ret==null)
				{
					ret=subret;
				}
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
