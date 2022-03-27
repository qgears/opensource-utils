package hu.qgears.emfcollab.backref;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.util.EContentAdapter;

/**
 * Content adapter similar to {@link EContentAdapter} but this works by adding a separate instance to each
 * notifier.
 */
abstract public class MultiInstanceContentAdapter extends EContentAdapter {
	@Override
	protected void addAdapter(Notifier notifier) {
		createAndAddAdapter(this, notifier); 
	}
	abstract protected Adapter createAndAddAdapter(MultiInstanceContentAdapter parent, Notifier notifier);
	@Override
	protected void removeAdapter(Notifier notifier) {
		MultiInstanceContentAdapter adapter=get(getClass(), notifier);
		notifier.eAdapters().remove(adapter);
	}
	public static <T extends MultiInstanceContentAdapter> T get(Class<T> adapterClass, Notifier notifier)
	{
		for(Adapter adapter: notifier.eAdapters())
		{
			if(adapterClass.isInstance(adapter))
			{
				return adapterClass.cast(adapter);
			}
		}
		return null;
	}
}
