package hu.qgears.emfcollab.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;

/**
 * EMF adapter to store an identifier on EObjects.
 */
public class IdAdapter implements Adapter {
	private Notifier target;
	private String id;
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
		return type==IdAdapter.class;
	}
	public static IdAdapter get(Notifier n)
	{
		for(Adapter ad: n.eAdapters())
		{
			if(ad instanceof IdAdapter)
			{
				return (IdAdapter) ad;
			}
		}
		IdAdapter ret=new IdAdapter();
		n.eAdapters().add(ret);
		return ret;
	}
	public void setId(String id) {
		this.id=id;
	}
	public String getId() {
		return id;
	}
}
