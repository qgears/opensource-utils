package hu.qgears.xtextgrammar;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;

public class TrackerAdapter extends EContentAdapter {
	@Override
	protected void addAdapter(Notifier notifier) {
		beforeAddingAdapter(notifier);
		super.addAdapter(notifier);
		if(notifier instanceof EObject)
		{
			CrossReferenceAdapter cra=CrossReferenceAdapter.getAllowNull((EObject) notifier);
			if(cra!=null)
			{
				cra.notifyAttachEvent(this);
			}
		}
	}
	protected void beforeAddingAdapter(Notifier notifier) {
	}
	@Override
	public void notifyChanged(Notification notification) {
		switch(notification.getEventType())
		{
		case Notification.SET:
			notificationSet(notification);
		}
		super.notifyChanged(notification);
	}
	protected void notificationSet(Notification notification) {
	}
}
