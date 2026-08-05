package hu.qgears.emfcollab.backref;

import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMIResource;

/**
 * Interface for querying inverse references within an EMF model structure.
 */
public interface EmfBackReference {	
	/**
	 * Get all references that end at this element by reference type.
	 * 
	 * @param target
	 * @param type
	 * @return
	 */
	Set<EmfReference> getTargetReferencesByType(EObject target, EReference type);
	/**
	 * Get all EMF references that target this element.
	 * 
	 * @param target
	 * @return
	 */
	Set<EmfReference> getTargetReferences(EObject target);
	/**
	 * Get all EMF references that source this element.
	 * These references are directly navigable: this method is useful for debug purpose only.
	 * @param source
	 * @return
	 */
	Set<EmfReference> getSourceReferences(EObject source);
	/**
	 * Returns all {@link EClass} instances (or types), that are present in
	 * underlying {@link XMIResource}.
	 * <p>
	 * NOTE this method doesn't find those {@link EClass}es, that are part of
	 * metamodel, but doesn't exist any of theirs instances in current
	 * {@link XMIResource}.
	 * 
	 * @return
	 */
	Set<EClass> getEclasses();
	/**
	 * Get all direct instances of an EMF class in the EMF model.
	 * <p>
	 * Instances of subclasses are not returned.
	 * 
	 * @param clazz Java interface class of the required type
	 * @return
	 */
	<T extends EObject> Set<T> getInstances(Class<T> clazz);
	/**
	 * Get all direct instances of an EMF class in the EMF model.
	 * <p>
	 * Instances of subclasses are not returned.
	 * 
	 * @param type EMF EClass type descriptor of the required type
	 * @return
	 */
	Set<EObject> getInstances(EClass type);
	/**
	 * Get all recursive instances of an EMF class in the EMF model.
	 * <p>
	 * Instances of subclasses are returned.
	 * 
	 * @param clazz Java interface class of the required type
	 * @return
	 */
	<T extends EObject> Set<T> getInstancesRecursive(Class<T> clazz);
	/**
	 * Get all recursive instances of an EMF class in the EMF model.
	 * <p>
	 * Instances of subclasses are returned.
	 * 
	 * @param type  EMF EClass type descriptor of the required type
	 * @return
	 */
	Set<EObject> getInstancesRecursive(EClass type);
	
	/**
	 * Initialize all indexes and tracking mechanism.
	 * Can be recalled multiple times, cache is rebuild in case of non-auto updated backref implementation.
	 */
	void initIndexes();
	/**
	 * Called when this instance is not necessary anymore.
	 * 
	 * @param resource
	 */
	void dispose();
	/**
	 * Prints all source and target references of the given {@link EObject} to
	 * standard output.
	 * 
	 * @param singleSelectedDomainObject
	 */
	void print(EObject singleSelectedDomainObject);
	/**
	 * Returns the model objects that have a reference to given target object.
	 * 
	 * @param target
	 * @return An ordered, never-null list containing the referring objects.
	 */
	Set<EObject> getReferrers(EObject target);
	/**
	 * Returns the single instance of the specified type, or <code>null</code> if
	 * there no instance exists at all, or multiple instances exist.
	 * 
	 * @param clz
	 * @return
	 */
	default <T extends EObject> T getSingleInstanceOf(Class<T> clz){
		Set<T> i = getInstancesRecursive(clz);
		if (i.size() == 1){
			return i.iterator().next();
		} else {
			return null;
		}
	}
	ResourceSet getResourceSet();
}
