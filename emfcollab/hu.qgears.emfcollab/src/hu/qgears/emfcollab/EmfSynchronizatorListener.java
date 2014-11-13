package hu.qgears.emfcollab;

import hu.qgears.emfcollab.backref.EmfBackReference;
import hu.qgears.emfcollab.util.UtilEmf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;


/**
 * Listens EMF model events
 * and records them into
 * serializable and remotely replayable objects.
 * @author rizsi
 *
 */
public class EmfSynchronizatorListener {
	private IdSource idSource;
	private List<EmfEvent> eventsCollected=new ArrayList<EmfEvent>();
	class MyAdapter extends EContentAdapter
	{
		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			EmfSynchronizatorListener.this.notifyChanged(notification);
		}
	}
	EmfBackReference emfBackRef=new EmfBackReference();
	MyAdapter adapter=new MyAdapter();
	public void notifyChanged(Notification notification) {
		int type=notification.getEventType();
		List<EmfEvent> events=new ArrayList<EmfEvent>();
		if(notification.getNotifier() instanceof EObject)
		{
			EObject eo=(EObject)notification.getNotifier();
			EStructuralFeature feature=(EStructuralFeature)notification.getFeature();
			Object newValue=notification.getNewValue();
			Object oldValue=notification.getOldValue();
			switch (type) {
			case Notification.ADD:
			case Notification.ADD_MANY:
			{
				buildSetReferenceEvents(
						notification, events,
						feature, newValue, oldValue);
			}
			break;
			case Notification.MOVE:
				createMoveEvent(events, eo, feature,
						(EObject)notification.getNewValue(),
						notification.getPosition(), (Integer)notification.getOldValue());
				break;
			case Notification.REMOVE_MANY:
			case Notification.REMOVE:
				if(feature instanceof EReference)
				{
					EReference ref=(EReference) feature;
					List<EObject> removed=getEObjects(oldValue);
					int position=notification.getPosition();
					createRemoveEvents(events, eo, ref, removed, position);
				}
				break;
			case Notification.SET:
			case Notification.UNSET:
				if(notification.getFeature() instanceof EAttribute)
				{
					createSetAttributeEvents(events, eo, (EAttribute)notification.getFeature(),
							notification.getNewValue(),
							notification.getOldValue());
				}else if(notification.getFeature() instanceof EReference)
				{
					buildSetReferenceEvents(notification, events,
							feature, newValue, oldValue);
				}
				break;
			default:
				break;
			}
		}
		eventsCollected.addAll(events);
	}
	private void buildSetReferenceEvents(
			Notification notification,
			List<EmfEvent> events,
			EStructuralFeature feature, Object newValue,
			Object oldValue) {
		EReference ref=(EReference) feature;
		List<EObject> objects=getEObjects(newValue);
		List<EObject> oldObjects=getEObjects(oldValue);
		EObject eo=(EObject)notification.getNotifier();
		if(ref.isContainment())
		{
			for(int i=0;i<oldObjects.size();++i)
			{
				EObject o=oldObjects.get(i);
				if(!objects.contains(o))
				{
					List<EObject> removed=new ArrayList<EObject>();
					removed.add(o);
					createRemoveEvents(events, eo, ref, removed, i);
				}
			}
			List<EmfEvent> setReferenceEvents=new ArrayList<EmfEvent>();
			buildObjects(events, setReferenceEvents, eo, ref, objects,
					notification.getPosition());
			events.addAll(setReferenceEvents);
		}else
		{
			buildSetReference(events, eo, ref, objects, oldObjects,
					notification.getPosition());
		}
	}
	/**
	 * Create an Event that represents the moving of an EMF object within
	 * a relation to a new position index.
	 * @param events
	 * @param eo
	 * @param feature
	 * @param newValue
	 * @param position
	 */
	private void createMoveEvent(List<EmfEvent> events, EObject eo,
			EStructuralFeature feature, EObject newValue, int position,
			int oldPosition) {
		EmfEventMove move=new EmfEventMove();
		move.setMovedId(getId(newValue));
		move.setParentId(getId(eo));
		move.setPosition(position);
		move.setReferenceName(feature.getName());
		move.setOldPosition(oldPosition);
		events.add(move);
	}
	private void createRemoveEvents(List<EmfEvent> events,
			EObject source, EReference ref,
			List<EObject> removed, int position) {
		for(EObject toRemove: removed)
		{
			if(ref.isContainment())
			{
				createDeleteEventRecursive(events, source, ref, toRemove, position);
			}else
			{
				EmfEventRemove removeEvent=new EmfEventRemove();
				removeEvent.setSourceId(getId(source));
				removeEvent.setReferenceName(ref.getName());
				removeEvent.setRemovedId(idSource.getId(resource, toRemove));
				removeEvent.setPosition(position);
				AEmfEvent ev=removeEvent;
				events.add(ev);
			}
			position++;
		}
	}
	private void createDeleteEventRecursive(List<EmfEvent> events,
			EObject source, EReference ref, EObject toRemove, int position) {
		List<EmfEvent> setReferenceEvents=new ArrayList<EmfEvent>();
		List<EmfEvent> deletionEvents=new ArrayList<EmfEvent>();
		createDeleteEvent(deletionEvents, setReferenceEvents, source, ref, toRemove, position);
		events.addAll(setReferenceEvents);
		events.addAll(deletionEvents);
	}
	private void createDeleteEvent(List<EmfEvent> events,
			List<EmfEvent> setReferenceEvents,
			EObject source,
			EReference ref, EObject toRemove, int position) {
		// TODO Before removing element the element
		// must be "unbuilt"
		// So undo can work fine
		deleteObjectParameters(events, setReferenceEvents, toRemove);
		EmfEventDelete removeEvent=new EmfEventDelete();
		removeEvent.setSourceId(getId(source));
		removeEvent.setReferenceName(ref.getName());
		removeEvent.setRemovedPosition(position);
		removeEvent.setDeletedType(typeToName(toRemove.eClass()));
		removeEvent.setDeletedId(idSource.getId(resource, toRemove));
		AEmfEvent ev=removeEvent;
		events.add(ev);
	}
	private void buildSetReference(List<EmfEvent> events, EObject eo,
			EReference ref, List<EObject> objects,
			List<EObject> oldObjects,
			int position) {
		String id=getId(eo);
		for(EObject o:objects)
		{
			EmfEventSetReference setRef=new EmfEventSetReference();
			setRef.setPosition(position);
			setRef.setParentId(id);
			setRef.setReferenceName(ref.getName());
			setRef.setCreatedId(getId(o));
			if(ref.getUpperBound()==1)
			{
				if(oldObjects.size()==1)
				{
					setRef.setOldId(getId(oldObjects.get(0)));
				}else
				{
					setRef.setOldId(null);
				}
			}
			events.add(setRef);
			position++;
		}
	}
	/**
	 * Create the events that build the clone of the elements in the list of
	 * objects.
	 * 
	 * @param events events gathering list that has to be filled
	 * @param setReferenceEvents events that set internal and external references of the object. These events must be executed _after_ all elements are created
	 * @param parent parent of the created elements
	 * @param reference the parent reference of the created objects
	 * @param objects the created objects
	 * @param pos position of the created object is the target list
	 */
	private void buildObjects(List<EmfEvent> events,
			List<EmfEvent> setReferenceEvents,
			EObject parent,
			EReference reference,
			List<EObject> objects,
			int pos) {
		for(EObject o: objects)
		{
			EmfEventCreate createEvent=new EmfEventCreate();
			createEvent.setParentId(getId(parent));
			createEvent.setReferenceName(reference.getName());
			createEvent.setPosition(pos);
			createEvent.setCreatedType(
					typeToName(o.eClass()));
			createEvent.setCreatedId(getId(o));
			events.add(createEvent);
			buildObjectParameters(events, setReferenceEvents, o);
			pos++;
		}
	}
	/**
	 * Create all events that set the current state of an object (that was just created).
	 * Parts:
	 *  * set all attributes
	 *  * set all references
	 *  * create all children
	 * @param events events gathering list that has to be filled
	 * @param setReferenceEvents events that set internal and external references of the object. These events must be executed _after_ all elements are created
	 * @param o
	 */
	private void buildObjectParameters(List<EmfEvent> events,
			List<EmfEvent> setReferenceEvents, EObject o) {
		EClass clazz=o.eClass();
//		List<EmfEvent> setReferenceEvents=new ArrayList<EmfEvent>();
		List<EStructuralFeature> l=clazz.getEAllStructuralFeatures();
		for(EStructuralFeature f: l)
		{
			if(f instanceof EReference)
			{
				EReference ref=(EReference) f;
				if(ref.isContainment())
				{
					Object newValue=o.eGet(ref);
					List<EObject> vals=getEObjects(newValue);
					if(vals.size()>0)
					{
						buildObjects(events, setReferenceEvents, o, ref, vals, 0);
					}
				}else if(!ref.isTransient()&&!ref.isDerived())
				{
					createReferenceSetterEvents(setReferenceEvents, o, ref);
				}
			}else if (f instanceof EAttribute)
			{
				EAttribute att=(EAttribute)f;
				if(o.eGet(att)!=null)
				{
					createSetAttributeEvents(events, o, att, o.eGet(att), att.getDefaultValue());
				}
			}
		}
		events.addAll(setReferenceEvents);
	}
	/**
	 * Create all events that set all parameters of an object to null.
	 * Parts:
	 *  * set all attributes to null
	 *  * set all references to null
	 *  * delete all children
	 * @param events events gathering list that has to be filled
	 * @param setReferenceEvents events that set internal and external references of the object. These events must be executed _after_ all elements are created
	 * @param o
	 */
	private void deleteObjectParameters(List<EmfEvent> events,
			List<EmfEvent> setReferenceEvents, EObject o) {
		EClass clazz=o.eClass();
		List<EStructuralFeature> l=clazz.getEAllStructuralFeatures();
		for(EStructuralFeature f: l)
		{
			if(f instanceof EReference)
			{
				EReference ref=(EReference) f;
				if(ref.isContainment())
				{
					Object toDelete=o.eGet(ref);
					List<EObject> vals=getEObjects(toDelete);
					for(EObject del: vals)
					{
						createDeleteEvent(events, setReferenceEvents, o, ref, del, 0);
					}
				}else if(!ref.isTransient()&&!ref.isDerived())
				{
					List<EObject> vals=UtilEmf.getReferenceValues(o, ref);
					if(vals.size()>0)
					{
						createRemoveEvents(setReferenceEvents, 
							o, ref, vals, 0);
					}
				}
			}else if (f instanceof EAttribute)
			{
				EAttribute att=(EAttribute)f;
				createSetAttributeEvents(setReferenceEvents, o, att, att.getDefaultValue(), o.eGet(att));
			}
		}
	}
	
	private void createSetAttributeEvents(List<EmfEvent> events, EObject o,
			EAttribute att, Object newValue, Object oldValue) {
		if(att.isTransient()||att.isDerived())
		{
			return;
		}
		EDataType dataType=att.getEAttributeType();
		EPackage p=dataType.getEPackage();
		String s=convertToString(p,dataType, newValue);
		String oldValStr=convertToString(p, dataType, oldValue);
		EmfEventSetAttribute setAttribute=new EmfEventSetAttribute();
		setAttribute.setObjectId(getId(o));
		setAttribute.setAttributeName(att.getName());
		setAttribute.setSerializedValue(s);
		setAttribute.setSerializedOldValue(oldValStr);
		setAttribute.setObjectTypeName(o.eClass().getInstanceTypeName());
		events.add(setAttribute);
//		System.out.println("REF: "+att.getName());
//		System.out.println("ATTVALSTRING: "+s);
//		Object recreated=p.getEFactoryInstance().createFromString(dataType, s);
//		System.out.println("recreated: "+recreated+" "+(recreated==null?"null":recreated.getClass()));
//		
//		
//		if(dataType instanceof EEnum)
//		{
//			int idx=((Enumerator)newValue).getValue();
//			EEnum type=(EEnum) dataType;
//			EEnumLiteral w=type.getELiterals().get(idx);
//			Object recreated=p.getEFactoryInstance().createFromString(dataType, w.getLiteral());
//			System.out.println(w.getClass().getName()+" "+w);
//			System.out.println("ENUM: "+att.getEAttributeType().getName()+" "+idx);
//			
//		}
//		System.out.println(""+newValue);
	}
	private String convertToString(EPackage p, EDataType dataType,
			Object newValue) {
		if(newValue==null)
		{
			return null;
		}
		return p.getEFactoryInstance().convertToString(dataType, newValue);
	}
	/**
	 * Create events for newly inserted element's non-containment references.
	 * @param setReferenceEvents
	 * @param o
	 * @param ref
	 */
	private void createReferenceSetterEvents(List<EmfEvent> setReferenceEvents,
			EObject o, EReference ref) {
		Object newValue=o.eGet(ref);
		List<EObject> vals=getEObjects(newValue);
		int position=0;
		String id=getId(o);
		for(EObject val:vals)
		{
			EmfEventSetReference setRef=new EmfEventSetReference();
			setRef.setPosition(position);
			setRef.setParentId(id);
			setRef.setReferenceName(ref.getName());
			setRef.setCreatedId(getId(val));
			setReferenceEvents.add(setRef);
			position++;
		}
	}
	/**
	 * Get the unique (in model but same on all clone instances) id
	 * of the model element.
	 * 
	 * Implementation returns XMI id.
	 * Id must be accessed (by all synchronizator code) through 
	 * this single point so later other (non XMI) backend may be used.
	 * 
	 * @param element
	 * @return the unique id of the model element
	 */
	private String getId(EObject element) {
		return idSource.getId(resource, element);
	}
	/**
	 * Convert an EClass to a string that can be used to instantiate an
	 * object on the remote EMF capable machine.
	 * @param clazz
	 * @return
	 */
	private String typeToName(EClass clazz)
	{
		String nsUri=clazz.getEPackage().getNsURI();
		String className=clazz.getName();
		return nsUri+":"+className;
	}
	/**
	 * Create a list of EObjects from an EMF notification value object.
	 * Check whether value is a single EObject or a list.
	 * @param newValue
	 * @return
	 */
	public static List<EObject> getEObjects(Object newValue) {
		if(newValue instanceof EObject)
		{
			return Collections.singletonList((EObject) newValue);
		}else if(newValue instanceof List<?>)
		{
			List<?> l=(List<?>)newValue;
			List<EObject> ret=new ArrayList<EObject>(l.size());
			for(Object o: l)
			{
				ret.add((EObject)o);
			}
			return ret;
		}
		return new ArrayList<EObject>(0);
	}
	ResourceSet resourceSet;
	Resource resource;
	public void init(Resource resource, IdSource idSource) {
		this.resource=resource;
		this.idSource=idSource;
		resourceSet=resource.getResourceSet();
		resource.eAdapters().add(adapter);
		emfBackRef.init(resource);
	}
	public List<EmfEvent> getAndClearEventsCollected() {
		List<EmfEvent> ret=eventsCollected;
		eventsCollected=new ArrayList<EmfEvent>();
		return ret;
	}
}
