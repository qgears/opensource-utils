package hu.qgears.emfcollab.backref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;

import hu.qgears.commons.MultiMapHashToHashSetImpl;
import hu.qgears.emfcollab.util.UtilEmf;
import hu.qgears.emfcollab.util.UtilVisitor;


/**
 * Indexes EMF model:
 *  * Maintain back references of all relations.
 *  * Maintain a type to instance mapping.
 */
public class EmfBackReferenceImpl implements EmfBackReference {
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
	class ReferencesAdapter implements Adapter
	{
		private Notifier target;
		private Set<EmfReferenceImpl> sources = new HashSet<>();
		private Set<EmfReferenceImpl> targets = new HashSet<>();
		@Override
		public void notifyChanged(Notification notification) {
		}
		@Override
		public Notifier getTarget() {
			return target;
		}
		public EObject getEObject()
		{
			return (EObject) target;
		}
		@Override
		public void setTarget(Notifier newTarget) {
			this.target=newTarget;
		}
		@Override
		public boolean isAdapterForType(Object type) {
			return type==ReferencesAdapter.class;
		}
	}
	private MyAdapter contentAdapter=new MyAdapter();
	private MultiMapHashToHashSetImpl<EClass, EObject> instances = new MultiMapHashToHashSetImpl<EClass, EObject>();
	private ResourceSet rs;
	/**
	 * Lazy init getter.
	 * @param n
	 * @return
	 */
	public ReferencesAdapter getAdapter(Notifier n) {
		for(Adapter adapter: n.eAdapters())
		{
			if(adapter instanceof ReferencesAdapter)
			{
				return (ReferencesAdapter)adapter;
			}
		}
		ReferencesAdapter ret=new ReferencesAdapter();
		n.eAdapters().add(ret);
		return ret;
	}
	private void removeReference(EObject o, EReference ref,
			Notification notification) {
		if (!ref.isTransient()&&!ref.isContainment()) {
			ReferencesAdapter srcAdapter=getAdapter(o);
			List<EObject> target = getEObjects(notification.getOldValue());
			for (EObject t : target) {
				ReferencesAdapter tgAdapter=getAdapter(t);
				EmfReferenceImpl emfRef = new EmfReferenceImpl(srcAdapter, ref, tgAdapter);
				removeReference(emfRef);
			}
		}
	}
	private void removeReference(EmfReferenceImpl emfRef) {
		ReferencesAdapter srcAdapter=emfRef.getSourceAdapter();
		ReferencesAdapter tgAdapter=emfRef.getTargetAdapter();
		srcAdapter.sources.remove(emfRef);
		tgAdapter.targets.remove(emfRef);
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
		if (!ref.isTransient()&&!ref.isContainment()) {
			ReferencesAdapter sourceAdapter=getAdapter(o);
			List<EObject> target;
			if(notification!=null)
			{
				target = getEObjects(notification.getNewValue());
			}else
			{
				target=UtilEmf.getReferenceValues(o, ref);
			}
			for (EObject t : target) {
				ReferencesAdapter targetAdapter=getAdapter(t);
				EmfReferenceImpl emfRef = new EmfReferenceImpl(sourceAdapter, ref, targetAdapter);
				ReferencesAdapter srcAdapter=getAdapter(o);
				ReferencesAdapter tgAdapter=getAdapter(t);
				srcAdapter.sources.add(emfRef);
				tgAdapter.targets.add(emfRef);
			}
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
	/**
	 * Call this method exactly once to start tracking the model.
	 * Only one resourceset can be tracked.
	 * @param rs
	 */
	public void init(ResourceSet rs) {
		this.rs=rs;
		rs.eAdapters().add(contentAdapter);
	}
	/**
	 * Remove an element('s all references) fully from the reference index.
	 * 
	 * @param element
	 * @param changed
	 *            list of elements changed by this method. Output parameter
	 */
	private void removeElement(EObject element) {
		ReferencesAdapter adapter=getAdapter(element);
		if(adapter!=null)
		{
			List<EmfReferenceImpl> toRemove = new ArrayList<EmfReferenceImpl>(adapter.sources
					.size()
					+ adapter.targets.size());
			toRemove.addAll(adapter.sources);
			toRemove.addAll(adapter.targets);
			for (EmfReferenceImpl ref : toRemove) {
				removeReference(ref);
			}
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
				addReference(element, ref, null);
			}
			instances.putSingle(element.eClass(), element);
		}
	}
	@Override
	public void print(EObject singleSelectedDomainObject) {
		ReferencesAdapter ra=getAdapter(singleSelectedDomainObject);
		for (EmfReferenceImpl ref : ra.sources) {
			System.out.println("SRC " + ref.getSource().eClass().getName()
					+ " " + ref.getTarget().eClass().getName() + " "
					+ ref.getRefType().getName());
		}
		for (EmfReferenceImpl ref : ra.targets) {
			System.out.println("TRG " + ref.getSource().eClass().getName()
					+ " " + ref.getTarget().eClass().getName() + " "
					+ ref.getRefType().getName());
		}
	}
	@Override
	public Set<EmfReference> getTargetReferencesByType(EObject target,
			EReference type) {
		ReferencesAdapter targetAdapter=getAdapter(target);
		Set<EmfReference> ret = new HashSet<>();
		for (EmfReferenceImpl ref : targetAdapter.targets) {
			if (ref.getRefType().equals(type)) {
				ret.add(ref);
			}
		}
		return ret;
	}

	@Override
	public Set<EmfReference> getTargetReferences(EObject target) {
		ReferencesAdapter targetAdapter=getAdapter(target);
		Set<EmfReference> ret = new HashSet<>();
		for (EmfReferenceImpl ref : targetAdapter.targets) {
			ret.add(ref);
		}
		return ret;
	}
	@Override
	public Set<EmfReference> getSourceReferences(EObject source) {
		ReferencesAdapter sourceAdapter=getAdapter(source);
		Set<EmfReference> ret = new HashSet<>();
		for (EmfReferenceImpl ref : sourceAdapter.sources) {
			ret.add(ref);
		}
		return ret;
	}
	@Override
	public Set<EObject> getInstances(EClass clazz) {
		return new HashSet<EObject>(instances.get(clazz));
	}
	@Override
	public Set<EObject> getInstancesRecursive(EClass clazz) {
		return getInstancesRecursive(clazz, EObject.class);
	}
	/**
	 * Type safe version of Get all recursive instances of an EMF class in the EMF model.
	 * <p>
	 * Instances of subclasses are returned.
	 * 
	 * @param clazz  EMF EClass type descriptor of the required type
	 * @param castTo the Java class that corresponds to the EMF EClass
	 * @return
	 */
	public <T> Set<T> getInstancesRecursive(EClass clazz, Class<T> castTo) {
		HashSet<T> ret = new HashSet<>();
		for (EClass c : instances.keySet()) {
			if (clazz.isSuperTypeOf(c)) {
				for(EObject o: instances.get(c))
				{
					ret.add(castTo.cast(o));
				}
			}
		}
		return ret;
	}
	@Override
	public Set<EClass> getEclasses() {
		return new HashSet<>(instances.keySet());
	}
	@Override
	public <T extends EObject> Set<T> getInstances(Class<T> type) {
		EClass clazz = UtilEmf.getEClassForJavaClass(type);
		Set<T> ret=new HashSet<>();
		for(EObject eo: instances.get(clazz))
		{
			ret.add(type.cast(eo));
		}
		return ret;
	}
	@Override
	public <T extends EObject> Set<T> getInstancesRecursive(Class<T> type) {
		EClass clazz = UtilEmf.getEClassForJavaClass(type);
		return getInstancesRecursive(clazz, type);
	}
	@Override
	public void initIndexes() {
		// Nothing to do - this implemention is automatic
	}
	@Override
	public void dispose() {
		rs.eAdapters().remove(contentAdapter);
	}
	@Override
	public Set<EObject> getReferrers(EObject target) {
		Set<EObject> ret=new HashSet<>();
		Set<EmfReference> refs = this.getTargetReferences(target);
		for (EmfReference r : refs){
			ret.add(r.getSource());
		}
		return ret;
	}
	@Override
	public ResourceSet getResourceSet() {
		return rs;
	}
}
