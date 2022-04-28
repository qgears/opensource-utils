package hu.qgears.xtextgrammar;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.Resource;

import hu.qgears.crossref.Doc;
import hu.qgears.parser.coloring.StyleBasedColoring;

/**
 * Adapter that manages cross reference information regarding the resource.
 */
public class ResourceCrossReferenceAdapter implements Adapter {
	private Notifier target;
	private Doc doc;
	private StyleBasedColoring coloring;
	@Override
	public void notifyChanged(Notification notification) {
	}
	@Override
	public Notifier getTarget() {
		return target;
	}
	@Override
	public void setTarget(Notifier newTarget) {
		this.target=newTarget;
	}
	@Override
	public boolean isAdapterForType(Object type) {
		return type==ResourceCrossReferenceAdapter.class;
	}
	public static ResourceCrossReferenceAdapter get(Resource r)
	{
		for(Adapter a: r.eAdapters())
		{
			if(a instanceof ResourceCrossReferenceAdapter)
			{
				return (ResourceCrossReferenceAdapter)a;
			}
		}
		ResourceCrossReferenceAdapter ret=new ResourceCrossReferenceAdapter();
		r.eAdapters().add(ret);
		return ret;
	}
	public void setDoc(Doc doc) {
		this.doc=doc;
	}
	public Doc getDoc() {
		return doc;
	}
	public Resource getResource() {
		return (Resource)target;
	}
	public String getDocId() {
		if(doc==null)
		{
			return "";
		}
		return doc.id;
	}
	public void setColoring(StyleBasedColoring coloring) {
		this.coloring = coloring;
	}
	public StyleBasedColoring getColoring() {
		return coloring;
	}
}
