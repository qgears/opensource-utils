package hu.qgears.emfcollab.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Descriptor;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

public class UtilEmf {
	private static Map<Class<?>, EClass> javaToEClassMap;
	/**
	 * Method only used when instrumenting for debugging.
	 * 
	 * @param resource
	 */
	static public void printContents(Resource resource, boolean printEdges) {
		System.out.println("Contents of the resource:");
		EList<EObject> objs = resource.getContents();
		for (EObject obj : objs) {
			printContents(System.out, obj, "", printEdges);
		}
	}
	/**
	 * Method only used when instrumenting for debugging.
	 * 
	 * @param out
	 * @param obj
	 * @param prefix
	 */
	public static void printContents(PrintStream out, EObject obj, String prefix, boolean printEdges) {
		out.println(prefix + obj);
		EList<EObject> objs = obj.eContents();
		if(printEdges)
		{
			printEdges(out, obj, prefix);
		}
		for (EObject sub : objs) {
			printContents(System.out, sub, prefix + "\t", printEdges);
		}
	}

	private static void printEdges(PrintStream out, EObject obj, String prefix) {
		EList<EReference> refs=obj.eClass().getEAllReferences();
		for(EReference ref:refs)
		{
			if(!ref.isContainment())
			{
				for(EObject o:getReferenceValues(obj, ref))
				{
					out.println(prefix+" * "+ref.getName()+": "+o);
				}
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static List<EObject> getReferenceValues(EObject element,
			EReference ref)
	{
		Object val=element.eGet(ref);
		if(val==null)
		{
			return Collections.EMPTY_LIST;
		}
		else if(val instanceof List<?>)
		{
			return (List<EObject>)val;
		}else if(val instanceof EObject)
		{
			return Collections.singletonList((EObject)val);
		}
		throw new RuntimeException("Error querying EMF element references");
	}

	private static class Error
	{
		EObject source;
		EReference reference;
		EObject target;
		public Error(EObject source, EReference reference, EObject target) {
			super();
			this.source = source;
			this.reference = reference;
			this.target = target;
		}
	}
	/**
	 * EMF models can contain dangling edges
	 * in some cases. This method deletes all dangling edges from the model.
	 * @param resource
	 */
	public static void sanitizeModel(Resource resource)
	{
		final List<Error> errors=new ArrayList<UtilEmf.Error>();
		for(EObject o:resource.getContents())
		{
			UtilVisitor.visitModel(o, 
				new UtilVisitor.Visitor(){
					@Override
					public Object visit(EObject element) {
						List<EReference> refs=element.eClass().getEAllReferences();
						for(EReference ref:refs)
						{
							if(!ref.isContainment()&&!ref.isDerived()
									&&!ref.isTransient())
							{
								Object val=element.eGet(ref);
								if(val instanceof EObject)
								{
									if(((EObject) val).eResource()==null)
									{
										errors.add(new Error(element, ref, (EObject)val));
									}
								}
								if(val instanceof Collection<?>)
								{
									for(Object o: (Collection<?>)val)
									{
										if(o instanceof EObject)
										{
											if(((EObject)o).eResource()==null)
											{
												errors.add(new Error(element,
														ref,(EObject)o));
											}
										}
									}
								}
							}
						}
						return null;
					}
				});
		}
		for(Error error:errors)
		{
			removeReference(error.source, error.reference, error.target);
		}
	}
	/**
	 * Remove a reference between two elements.
	 * @param source
	 * @param reference
	 * @param target
	 */
	public static void removeReference(EObject source, EReference reference,
			EObject target) {
		Object o=source.eGet(reference);
		if(o instanceof Collection<?>)
		{
			((Collection<?>) o).remove(target);
		}else
		{
			source.eSet(reference, null);
		}
	}
	/**
	 * Returns the {@link EClass} representation of the given Java type. Only
	 * types defined in {@link ModelPackage} are supported.
	 * 
	 * @param javaClass
	 *            The Java class describing the type of the model object
	 * @return The corresponding {@link EClass}, or <code>null</code> if the
	 *         type cannot be found in {@link ModelPackage}.
	 */
	public static EClass getEClassForJavaClass(Class<? extends EObject> javaClass) {
		if (javaToEClassMap == null) {
			initMap();
		}
		return javaToEClassMap.get(javaClass);
	}
	public static synchronized void initMap() {
		if (javaToEClassMap == null) {
			javaToEClassMap = new HashMap<>();
			for (Entry<String, Object> pReg : EPackage.Registry.INSTANCE.entrySet()) {
				// initializing packages from com.bbraun namespace (guidsl, SLD
				// / FSM models, if registered)
				if (pReg.getKey().contains("com.bbraun.")) {
					addPackage(pReg.getValue());
				}
			}
		}
	}
	private static void addPackage(Object pkg) {
		EPackage epkg;
		if (pkg instanceof EPackage) {
			epkg = (EPackage) pkg;
		} else if (pkg instanceof Descriptor) {
			Descriptor descriptor = (Descriptor) pkg;
			epkg = descriptor.getEPackage();
		} else {
			throw new RuntimeException("Unhandled EMF package registry kind: " + pkg);
		}
		initMapForPackage(epkg);
	}
	private static void initMapForPackage(EPackage packageToInit) {
		for (EObject clazz : packageToInit.eContents()) {
			if (clazz instanceof EClass) {
				javaToEClassMap.put(((EClass) clazz).getInstanceClass(), (EClass) clazz);
			}
		}
	}
}
