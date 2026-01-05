package hu.qgears.emfcollab.backref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;

import hu.qgears.commons.MultiMapHashImpl;
import hu.qgears.commons.MultiMapHashToHashSetImpl;
import hu.qgears.commons.UtilEventListener;
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
			Object notifier=notification.getNotifier();
			if (notifier instanceof EObject) {
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
							// addElement(o);
							addElementRecursive(notification);
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
							addElementRecursive(notification);
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
			}else if(notifier instanceof Resource)
			{
				switch (notification.getEventType()) {
				case Notification.ADD:
					// EObject newContent=(EObject)notification.getNewValue();
					addElementRecursive(notification);
					break;
				case Notification.REMOVE:
					EObject oldContent=(EObject)notification.getOldValue();
					removeElement(oldContent);
					break;
				}
				// System.out.println("R notif: "+notification);
			}else if(notifier instanceof ResourceSet)
			{
				switch (notification.getEventType()) {
				case Notification.REMOVE:
					Resource oldContent=(Resource)notification.getOldValue();
					for(EObject eo: oldContent.getContents())
					{
						removeElementRecursive(eo);
					}
					// System.out.println("Remove Resource: "+oldContent);
					// removeElement(oldContent);
					break;
				}
				// System.out.println("RS notif: "+notification);
			}
		}

		public EmfBackReferenceImpl getHost() {
			return EmfBackReferenceImpl.this;
		}
	}
	public class EmfObjectReferencesAdapter implements Adapter, EmfObjectReferences
	{
		private Notifier target;
		private Map<EmfReferenceImpl, EmfReferenceImpl> sources = new HashMap<>();
		private Map<EmfReferenceImpl, EmfReferenceImpl> targets = new HashMap<>();
		private MultiMapHashImpl<EStructuralFeature, UtilEventListener<Notification>> listeners=null;
		@Override
		public void notifyChanged(Notification notification) {
			if(listeners!=null)
			{
				if (notification.getNotifier() instanceof EObject) {
					Object feature = notification.getFeature();
					if (feature instanceof EStructuralFeature) {
						List<UtilEventListener<Notification>> l=listeners.getPossibleNull((EStructuralFeature)feature);
						if(l!=null)
						{
							for(UtilEventListener<Notification> note: l)
							{
								note.eventHappened(notification);
							}
						}
					}
				}
			}
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
			return type==EmfObjectReferencesAdapter.class;
		}
		@Override
		public void addReferenceListener(EStructuralFeature ref, UtilEventListener<Notification> event, boolean fireNow) {
			if(listeners==null)
			{
				listeners=new MultiMapHashImpl<>();
			}
			listeners.putSingle(ref, event);
			if(fireNow)
			{
				event.eventHappened(null);
			}
		}
	}
	private MyAdapter contentAdapter=new MyAdapter();
	private MultiMapHashToHashSetImpl<EClass, EObject> instances = new MultiMapHashToHashSetImpl<EClass, EObject>();
	private ResourceSet rs;
	/**
	 * Get the references adapter for an object.
	 * Lazy init getter.
	 * @param n
	 * @return
	 */
	public EmfObjectReferencesAdapter getAdapter(Notifier n) {
		for(Adapter adapter: n.eAdapters())
		{
			if(adapter instanceof EmfObjectReferencesAdapter)
			{
				return (EmfObjectReferencesAdapter)adapter;
			}
		}
		EmfObjectReferencesAdapter ret=new EmfObjectReferencesAdapter();
		n.eAdapters().add(ret);
		return ret;
	}
	/**
	 * Addapter that can be used to listen Reference target values.
	 * @param n
	 * @return
	 */
	public EmfObjectReferences getEmfReferencesAdapter(Notifier n)
	{
		return getAdapter(n);
	}
	private void removeReference(EObject o, EReference ref,
			Notification notification) {
		if (!ref.isTransient()&&!ref.isContainment()) {
			EmfObjectReferencesAdapter srcAdapter=getAdapter(o);
			List<EObject> target = getEObjects(notification.getOldValue());
			for (EObject t : target) {
				EmfObjectReferencesAdapter tgAdapter=getAdapter(t);
				EmfReferenceImpl emfRef = new EmfReferenceImpl(srcAdapter, ref, tgAdapter);
				removeReference(emfRef);
			}
		}
	}
	private void removeReference(EmfReferenceImpl emfRef) {
		EmfObjectReferencesAdapter srcAdapter=emfRef.getSourceAdapter();
		EmfObjectReferencesAdapter tgAdapter=emfRef.getTargetAdapter();
		srcAdapter.sources.remove(emfRef);
		tgAdapter.targets.remove(emfRef);
	}
	private void removeElementRecursive(EObject o, EReference ref,
			Notification notification) {
		List<EObject> target = getEObjects(notification.getOldValue());
		for (EObject t : target) {
			removeElementRecursive(t);
		}
	}
	private void removeElementRecursive(EObject t) {
		UtilVisitor.visitModel(t, new UtilVisitor.Visitor() {
			@Override
			public Object visit(EObject element) {
				removeElement(element);
				return null;
			}
		});
	}
	protected void addElementRecursive(Notification notification) {
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
			EmfObjectReferencesAdapter sourceAdapter=getAdapter(o);
			List<EObject> target;
			if(notification!=null)
			{
				target = getEObjects(notification.getNewValue());
			}else
			{
				target=UtilEmf.getReferenceValues(o, ref);
			}
			for (EObject t : target) {
				EmfObjectReferencesAdapter targetAdapter=getAdapter(t);
				EmfReferenceImpl emfRef = new EmfReferenceImpl(sourceAdapter, ref, targetAdapter);
				EmfObjectReferencesAdapter srcAdapter=getAdapter(o);
				EmfObjectReferencesAdapter tgAdapter=getAdapter(t);
				srcAdapter.sources.put(emfRef, emfRef);
				tgAdapter.targets.put(emfRef, emfRef);
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
	public static EmfBackReferenceImpl getByEobject(EObject eo)
	{
		for(Adapter a: eo.eAdapters())
		{
			if(a instanceof MyAdapter)
			{
				return ((MyAdapter) a).getHost();
			}
		}
		return null;
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
		EmfObjectReferencesAdapter adapter=getAdapter(element);
		if(adapter!=null)
		{
			List<EmfReferenceImpl> toRemove = new ArrayList<EmfReferenceImpl>(adapter.sources
					.size()
					+ adapter.targets.size());
			toRemove.addAll(adapter.sources.keySet());
			toRemove.addAll(adapter.targets.keySet());
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
	protected void addElement(EObject element) {
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
		EmfObjectReferencesAdapter ra=getAdapter(singleSelectedDomainObject);
		for (EmfReferenceImpl ref : ra.sources.keySet()) {
			System.out.println("SRC " + ref.getSource().eClass().getName()
					+ " " + ref.getTarget().eClass().getName() + " "
					+ ref.getRefType().getName());
		}
		for (EmfReferenceImpl ref : ra.targets.keySet()) {
			System.out.println("TRG " + ref.getSource().eClass().getName()
					+ " " + ref.getTarget().eClass().getName() + " "
					+ ref.getRefType().getName());
		}
	}
	@Override
	public Set<EmfReference> getTargetReferencesByType(EObject target,
			EReference type) {
		EmfObjectReferencesAdapter targetAdapter=getAdapter(target);
		Set<EmfReference> ret = new HashSet<>();
		for (EmfReferenceImpl ref : targetAdapter.targets.keySet()) {
			if (ref.getRefType().equals(type)) {
				ret.add(ref);
			}
		}
		return ret;
	}

	@Override
	public Set<EmfReference> getTargetReferences(EObject target) {
		EmfObjectReferencesAdapter targetAdapter=getAdapter(target);
		Set<EmfReference> ret = new HashSet<>();
		for (EmfReferenceImpl ref : targetAdapter.targets.keySet()) {
			ret.add(ref);
		}
		return ret;
	}
	@Override
	public Set<EmfReference> getSourceReferences(EObject source) {
		EmfObjectReferencesAdapter sourceAdapter=getAdapter(source);
		Set<EmfReference> ret = new HashSet<>();
		for (EmfReferenceImpl ref : sourceAdapter.sources.keySet()) {
			ret.add(ref);
		}
		return ret;
	}
	@Override
	public Set<EObject> getInstances(EClass clazz) {
		HashSet<EObject> ret=instances.getPossibleNull(clazz);
		if(ret!=null)
		{
			return new HashSet<EObject>(instances.get(clazz));
		}else
		{
			return new HashSet<EObject>();
		}
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
				HashSet<EObject> l=instances.getPossibleNull(c);
				if(l!=null)
				{
					for(EObject o: l)
					{
						ret.add(castTo.cast(o));
					}
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
		HashSet<EObject> contained = instances.getPossibleNull(clazz);
		if(contained!=null)
		{
			for(EObject eo: contained)
			{
				ret.add(type.cast(eo));
			}
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
	public EmfReferenceImpl getSourceReference(EObject o, EReference r, EObject tg, int index) {
		EmfObjectReferencesAdapter sourceAdapter=getAdapter(o);
		EmfObjectReferencesAdapter targetAdapter=getAdapter(tg);
		EmfReferenceImpl ri=new EmfReferenceImpl(sourceAdapter, r, targetAdapter);
		return sourceAdapter.sources.get(ri);
	}
}
