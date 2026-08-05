package hu.qgears.emfcollab;

import hu.qgears.emfcollab.impl.LoadedResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;


/**
 * Executes serializable EMF commands on an emf resource.
 * @author rizsi
 *
 */
public class EmfCommandExecutor {
	Resource resource;
	ResourceSet resourceSet;
	IdSource idSource;
	public void init(LoadedResource loadedResource)
	{
		this.resource=loadedResource.getResource();
		this.resourceSet=resource.getResourceSet();
		this.idSource=loadedResource.getIdSoruce();
	}
	/**
	 * Execute a serializable EMF events on the EMF resource.
	 * @param events
	 */
	public void executeEvents(
			List<EmfEvent> events
			)
	{
		for(EmfEvent event:events)
		{
			executeEvent(event);
		}
	}
	/**
	 * Undo serializable EMF events on the EMF resource.
	 * 
	 * The events on the list are undone in reverse order.
	 * @param events
	 */
	public void undoEvents(
			List<EmfEvent> events
			)
	{
		List<EmfEvent> rev=new ArrayList<EmfEvent>(events);
		Collections.reverse(rev);
		for(EmfEvent event:rev)
		{
			undoEvent(event);
		}
	}
	/**
	 * Execute a serializable EMF event on the EMF resource.
	 * @param event
	 */
	public void executeEvent(
			EmfEvent event
			)
	{
		try
		{
		switch (event.getType()) {
		case create:
			executeCreate((EmfEventCreate) event);
			break;
		case setAttribute:
			executeSetAttribute((EmfEventSetAttribute) event);
			break;
		case move:
			executeMove((EmfEventMove)event);
			break;
		case remove:
			executeRemove((EmfEventRemove)event);
			break;
		case delete:
			executeDelete((EmfEventDelete)event);
			break;
		case setReference:
			executeSetReference((EmfEventSetReference) event);
			break;
		default:
			break;
		}
		}catch (Exception e) {
			logError("Executing event", e);
		}
	}
	private void logError(String msg, Exception e) {
		System.err.println(msg);
		e.printStackTrace();
	}
	/**
	 * Undo a serializable EMF event on the EMF resource.
	 * @param event
	 */
	public void undoEvent(
			EmfEvent event
			)
	{
		try
		{
		switch (event.getType()) {
		case create:
			undoCreate((EmfEventCreate) event);
			break;
		case setAttribute:
			undoSetAttribute((EmfEventSetAttribute) event);
			break;
		case move:
			undoMove((EmfEventMove)event);
			break;
		case remove:
			undoRemove((EmfEventRemove)event);
			break;
		case delete:
			undoDelete((EmfEventDelete)event);
			break;
		case setReference:
			undoSetReference((EmfEventSetReference) event);
			break;
		default:
			break;
		}
		}catch(Exception e)
		{
			logError("Undoing event", e);
		}
	}

	private void undoSetReference(EmfEventSetReference event) {
		EObject source=idSource.getById(event.getParentId());
		if(source==null)
		{
			logElementDoesNotExistError("Undo set reference", event.getParentId());
			return;
		}
		String refName=event.getReferenceName();
		EReference ref=(EReference)source.eClass().getEStructuralFeature(refName);
		if(ref.getUpperBound()==1)
		{
			String oldId=event.getOldId();
			EObject oldObj=null;
			if(oldId!=null)
			{
				oldObj=idSource.getById(oldId);
			}
			source.eSet(ref, oldObj);
		}else
		{
			String id=event.getCreatedId();
			EObject obj=idSource.getById(id);
			if(obj!=null)
			{
				removeFromEmfReference(source, ref, obj);
			}
		}
	}
	private void undoDelete(EmfEventDelete event) {
		try {
			String fqId=event.getDeletedType();
			EClass clazz=(EClass) getEClassifierFromId(fqId);
			EObject created=clazz.getEPackage().getEFactoryInstance().create(clazz);
			idSource.setId(created, event.getDeletedId());
			EObject parent=idSource.getById(event.getSourceId());
			EReference ref=(EReference)parent.eClass().getEStructuralFeature(event.getReferenceName());
			setRerefence(parent, ref, created, event.getRemovedPosition());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String fqId=event.getDeletedType();
			EClass clazz=(EClass) getEClassifierFromId(fqId);
			EObject created=clazz.getEPackage().getEFactoryInstance().create(clazz);
			idSource.setId(created, event.getDeletedId());
			EObject parent=idSource.getById(event.getSourceId());
			EReference ref=(EReference)parent.eClass().getEStructuralFeature(event.getReferenceName());
			setRerefence(parent, ref, created, event.getRemovedPosition());
		}
	}
	private void undoRemove(EmfEventRemove event) {
		EObject source=idSource.getById(event.getSourceId());
		EObject target=idSource.getById(event.getRemovedId());
		String refName=event.getReferenceName();
		EReference ref=(EReference)source.eClass().getEStructuralFeature(refName);
		setRerefence(source, ref, target, event.getPosition());
	}
	private void undoMove(EmfEventMove event) {
		EObject moved=idSource.getById(event.getMovedId());
		EObject parent=idSource.getById(event.getParentId());
		EReference ref=(EReference)parent.eClass().getEStructuralFeature(event.getReferenceName());
		moveInRerefence(parent, ref, moved, event.getOldPosition());
	}
	private void undoSetAttribute(EmfEventSetAttribute event) {
		String objectId=event.getObjectId();
		EObject o=idSource.getById(objectId);
		if(o==null)
		{
			logElementDoesNotExistError("Undo set attribute", objectId);
			return;
		}
		EAttribute att=(EAttribute)o.eClass().getEStructuralFeature(event.getAttributeName());
		String val=event.getSerializedOldValue();
			
		EDataType dataType=att.getEAttributeType();
		EPackage p=dataType.getEPackage();
		Object recreated=parseObject(p, dataType, val);
		try
		{
			o.eSet(att, recreated);
		}catch(Exception e)
		{
			System.err.println("Known bug");
			// TODO known bug in EMFCollab
			// see executeSetAttribute
		}
	}
	private void logElementDoesNotExistError(String string, String objectId) {
		System.out.println("Para - element does not exist in command "+string+": "+objectId);
		System.out.flush();
	}
	private void executeSetReference(EmfEventSetReference event) {
		EObject source=idSource.getById(event.getParentId());
		EObject target=idSource.getById(event.getCreatedId());
		String refName=event.getReferenceName();
		if(source==null)
		{
			logElementDoesNotExistError("Set reference", event.getParentId());
			return;
		}
		EReference ref=(EReference)source.eClass().getEStructuralFeature(refName);
		setRerefence(source, ref, target, event.getPosition());
	}
	private void executeRemove(EmfEventRemove event) {
		String sourceId=event.getSourceId();
		EObject source=idSource.getById(sourceId);
		if(source!=null)
		{
			EReference ref=(EReference) source.eClass().getEStructuralFeature(event.getReferenceName());
			String id=event.getRemovedId();
			EObject obj=idSource.getById(id);
			if(obj!=null)
			{
				removeFromEmfReference(source, ref, obj);
			}
		}
	}
	private void executeDelete(EmfEventDelete event) {
		String sourceId=event.getSourceId();
		EObject source=idSource.getById(sourceId);
		if(source!=null)
		{
			EReference ref=(EReference) source.eClass().getEStructuralFeature(event.getReferenceName());
			EObject obj=getDeletedObject(event, resource, idSource);
			if(obj!=null)
			{
				removeFromEmfReference(source, ref, obj);
			}
		}
	}
	
	public static EObject getDeletedObject(EmfEventDelete event,
			Resource resource,
			IdSource idSource)
	{
		String sourceId=event.getSourceId();
		EObject source=idSource.getById(sourceId);
		int pos=event.getRemovedPosition();
		if(pos<0)
		{
			pos=0;
		}
		EReference ref=(EReference) source.eClass().getEStructuralFeature(event.getReferenceName());
		List<EObject> objs=EmfSynchronizatorListener.getEObjects(source.eGet(ref));
		EObject obj=objs.get(pos);
		return obj;
	}
	
	@SuppressWarnings("rawtypes")
	public static void removeFromEmfReference(
			EObject parent, EReference ref,
			EObject objectToDelete) {
		Object oldContainment=parent.eGet(ref);
		if(oldContainment instanceof List)
		{
			List list=(List)oldContainment;
			list.remove(objectToDelete);
		}else
		{
			parent.eSet(ref, null);
		}
	}
	private void executeMove(EmfEventMove event) {
		EObject moved=idSource.getById(event.getMovedId());
		EObject parent=idSource.getById(event.getParentId());
		EReference ref=(EReference)parent.eClass().getEStructuralFeature(event.getReferenceName());
		moveInRerefence(parent, ref, moved, event.getPosition());
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void moveInRerefence(EObject parent, EReference ref, EObject moved,
			int position) {
		Object oldValue=parent.eGet(ref);
		if(oldValue instanceof EList)
		{
			EList refList=(EList)oldValue;
			refList.move(position, moved);
		}
	}
	private void executeSetAttribute(EmfEventSetAttribute event) {
		String objectId=event.getObjectId();
		EObject o=idSource.getById(objectId);
		if(o==null)
		{
			logElementDoesNotExistError("Set Attribute", objectId);
			return;
		}
			EAttribute att=(EAttribute)o.eClass().getEStructuralFeature(event.getAttributeName());
			String val=event.getSerializedValue();
			
			EDataType dataType=att.getEAttributeType();
			EPackage p=dataType.getEPackage();
			Object recreated=parseObject(p, dataType, val);
			try {
				o.eSet(att, recreated);
			} catch (Exception e) {
				System.err.println("EMFCollab Known bug - can not set attribute");
				// TODO in some cases null is not allowed to set though EMFCollab uses this trick to reset the value of the element
			}
	}
	private Object parseObject(EPackage p, EDataType dataType, String val) {
		if(val==null)
		{
			return null;
		}
		return p.getEFactoryInstance().createFromString(dataType, val);
	}
	private void executeCreate(EmfEventCreate event) {
		String fqId=event.getCreatedType();
		EClass clazz=(EClass) getEClassifierFromId(fqId);
		EObject created=clazz.getEPackage().getEFactoryInstance().create(clazz);
		idSource.setId(created, event.getCreatedId());
		EObject parent=idSource.getById(event.getParentId());
		EReference ref=(EReference)parent.eClass().getEStructuralFeature(event.getReferenceName());
		setRerefence(parent, ref, created, event.getPosition());
	}
	
	private void undoCreate(EmfEventCreate event) {
		EObject eo=idSource.getById(event.getCreatedId());
		EObject parent=eo.eContainer();
		EReference ref=(EReference) parent.eClass().getEStructuralFeature(event.getReferenceName());
		removeFromEmfReference(parent, ref, eo);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setRerefence(EObject source, EReference ref,
			EObject target,
			int position) {
		Object oldValue=source.eGet(ref);
		if(oldValue instanceof List)
		{
			List refList=(List)oldValue;
			if(position<0)
			{
				position=refList.size();
			}
			if(position>refList.size())
			{
				// TODO this hacks out some exceptions that sometimes come from EmfServer
				position=refList.size();
			}
			if(!refList.contains(target))
			{
				refList.add(position, target);
			}
		}else
		{
			source.eSet(ref, target);
		}
	}
	/**
	 * Resolve an fqId of a class to an EClassifier instance in the context of the current resourceset.
	 * @param fqId
	 * @return
	 */
	private EClassifier getEClassifierFromId(String fqId)
	{
		int idx=fqId.lastIndexOf(':');
		String nsUri=fqId.substring(0, idx);
		String className=fqId.substring(idx+1);
		return getEClassifierFromId(nsUri, className);
	}
	/**
	 * Resolve an fqId of a class to an EClassifier instance in the context of the current resourceset.
	 * @param nsUri
	 * @param className
	 * @return
	 */
	private EClassifier getEClassifierFromId(String nsUri, String className)
	{
		List<EClassifier> l=resourceSet.getPackageRegistry().getEPackage(nsUri)
			.getEClassifiers();
		for(EClassifier cl:l)
		{
			if(cl.getName().equals(className))
			{
				return cl;
			}
		}
		return null;
	}
}
