package hu.qgears.emfcollab.backref;

import hu.qgears.commons.MultiMapHashToHashSetImpl;
import hu.qgears.emfcollab.load.UtilVisitor;
import hu.qgears.emfcollab.util.UtilEmf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;


/**
 * Indexes EMF model:
 *  * Maintain back references of all relations.
 *  * Maintain a type to instance mapping.
 * 
 * @author rizsi
 * 
 */
public class EmfBackReference {
	class MyAdapter extends EContentAdapter {
		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getNotifier() instanceof EObject) {
				EObject o = (EObject) notification.getNotifier();
				Object feature = notification.getFeature();
				if (feature instanceof EReference) {
					EReference ref = (EReference) feature;
					boolean containment = ref.isContainment();
					switch (notification.getEventType()) {
					case Notification.ADD:
					case Notification.ADD_MANY:
						addReference(o, ref, notification);
						if (containment) {
							addElementRecursive(o, ref, notification);
						}
						break;
					case Notification.MOVE:
						break;
					case Notification.REMOVE_MANY:
					case Notification.REMOVE:
						removeReference(o, ref, notification);
						if (containment) {
							removeElementRecursive(o, ref, notification);
						}
						break;
					case Notification.SET:
						// TODO remove old Value
						removeReference(o, ref, notification);
						addReference(o, ref, notification);
						if (containment) {
							addElementRecursive(o, ref, notification);
						}
						break;
					case Notification.UNSET:
						removeReference(o, ref, notification);
						if (containment) {
							removeElementRecursive(o, ref, notification);
						}
						break;
					}

				}
			}
		}

	}

	private void removeReference(EObject o, EReference ref,
			Notification notification) {
		List<EObject> target = getEObjects(notification.getOldValue());
		for (EObject t : target) {
			EmfReference emfRef = new EmfReference(o, ref, t);
			removeReference(emfRef);
		}
	}

	private void removeElementRecursive(EObject o, EReference ref,
			Notification notification) {
		List<EObject> target = getEObjects(notification.getOldValue());
		for (EObject t : target) {
			UtilVisitor.visitModel(t, new UtilVisitor.Visitor() {
				@Override
				public Object visit(EObject element) {
					removeElement(element);
					return null;
				}
			});
		}
	}

	private void addElementRecursive(EObject o, EReference ref,
			Notification notification) {
		List<EObject> target = getEObjects(notification.getNewValue());
		for (EObject t : target) {
			UtilVisitor.visitModel(t, new UtilVisitor.Visitor() {
				@Override
				public Object visit(EObject element) {
					addElement(element);
					return null;
				}
			});
		}
	}

	private void addReference(EObject o, EReference ref,
			Notification notification) {
		List<EObject> target = getEObjects(notification.getNewValue());
		for (EObject t : target) {
			EmfReference emfRef = new EmfReference(o, ref, t);
			addReference(emfRef);
		}
	}

	/**
	 * Create a list of EObjects from an EMF notification value object. Check
	 * whether value is a single EObject or a list.
	 * 
	 * @param newValue
	 * @return
	 */
	public static List<EObject> getEObjects(Object newValue) {
		if (newValue instanceof EObject) {
			return Collections.singletonList((EObject) newValue);
		} else if (newValue instanceof List<?>) {
			List<?> l = (List<?>) newValue;
			List<EObject> ret = new ArrayList<EObject>(l.size());
			for (Object o : l) {
				ret.add((EObject) o);
			}
			return ret;
		}
		return new ArrayList<EObject>(0);
	}

	MyAdapter adapter = new MyAdapter();

	MultiMapHashToHashSetImpl<EClass, EObject> instances = new MultiMapHashToHashSetImpl<EClass, EObject>();
	MultiMapHashToHashSetImpl<EObject, EmfReference> sourceReferences = new MultiMapHashToHashSetImpl<EObject, EmfReference>();
	MultiMapHashToHashSetImpl<EObject, EmfReference> targetReferences = new MultiMapHashToHashSetImpl<EObject, EmfReference>();
	MultiMapHashToHashSetImpl<EReference, EmfReference> references = new MultiMapHashToHashSetImpl<EReference, EmfReference>();

	public void init(Resource resource) {
		resource.eAdapters().add(adapter);
		for (EObject o : resource.getContents()) {
			UtilVisitor.visitModel(o, new UtilVisitor.Visitor() {
				@Override
				public Object visit(EObject element) {
					addElement(element);
					return null;
				}
			});
		}
	}

	/**
	 * Remove an element('s all references) fully from the reference index.
	 * 
	 * @param element
	 * @param changed
	 *            list of elements changed by this method. Output parameter
	 */
	private void removeElement(EObject element) {
		Set<EmfReference> sources = sourceReferences.get(element);
		Set<EmfReference> targets = targetReferences.get(element);
		List<EmfReference> toRemove = new ArrayList<EmfReference>(sources
				.size()
				+ targets.size());
		toRemove.addAll(sources);
		toRemove.addAll(targets);
		for (EmfReference ref : toRemove) {
			removeReference(ref);
		}
		instances.removeSingle(element.eClass(), element);
	}

	/**
	 * Add an element's all references (that are source of the reference) to the
	 * index model.
	 * 
	 * @param element
	 */
	private void addElement(EObject element) {
		if (element.eResource() != null) {
			EList<EReference> refs = element.eClass().getEAllReferences();
			for (EReference ref : refs) {
				if (!ref.isTransient()) {
					List<EObject> targets = UtilEmf.getReferenceValues(
							element, ref);
					for (EObject target : targets) {
						EmfReference r = new EmfReference(element, ref, target);
						addReference(r);
					}
				}
			}
			instances.putSingle(element.eClass(), element);
		}
	}

	void addReference(EmfReference ref) {
		sourceReferences.putSingle(ref.getSource(), ref);
		targetReferences.putSingle(ref.getTarget(), ref);
		references.putSingle(ref.getRefType(), ref);
	}

	void removeReference(EmfReference ref) {
		sourceReferences.removeSingle(ref.getSource(), ref);
		targetReferences.removeSingle(ref.getTarget(), ref);
		references.removeSingle(ref.getRefType(), ref);
	}

	public void print(Object singleSelectedDomainObject) {
		for (EmfReference ref : sourceReferences
				.get(singleSelectedDomainObject)) {
			System.out.println("SRC " + ref.getSource().eClass().getName()
					+ " " + ref.getTarget().eClass().getName() + " "
					+ ref.getRefType().getName());
		}
		for (EmfReference ref : targetReferences
				.get(singleSelectedDomainObject)) {
			System.out.println("TRG " + ref.getSource().eClass().getName()
					+ " " + ref.getTarget().eClass().getName() + " "
					+ ref.getRefType().getName());
		}
	}

	/**
	 * Get all references that end at this element by reference type.
	 * 
	 * @param target
	 * @param type
	 * @return
	 */
	public Set<EmfReference> getTargetReferencesByType(EObject target,
			EReference type) {
		Set<EmfReference> query = targetReferences.get(target);
		Set<EmfReference> ret = new HashSet<EmfReference>();
		for (EmfReference ref : query) {
			if (ref.getRefType().equals(type)) {
				ret.add(ref);
			}
		}
		return ret;
	}

	/**
	 * Get all EMF references that target this element.
	 * 
	 * @param target
	 * @return
	 */
	public Set<EmfReference> getTargetReferences(EObject target) {
		Set<EmfReference> query = targetReferences.get(target);
		Set<EmfReference> ret = new HashSet<EmfReference>();
		for (EmfReference ref : query) {
			ret.add(ref);
		}
		return ret;
	}

	/**
	 * Get all direct instances of an EMF class in the EMF model.
	 * 
	 * @param clazz
	 * @return
	 */
	public Set<EObject> getInstances(EClass clazz) {
		return new HashSet<EObject>(instances.get(clazz));
	}

	/**
	 * Get all recursive instances of an EMF class in the EMF model.
	 * 
	 * @param clazz
	 * @return
	 */
	public Set<EObject> getInstancesRecursive(EClass clazz) {
		HashSet<EObject> ret = new HashSet<EObject>();
		for (EClass c : instances.keySet()) {
			if (clazz.isSuperTypeOf(c)) {
				ret.addAll(instances.get(c));
			}
		}
		return ret;
	}
}
