package hu.qgears.crossref;

import java.util.ArrayList;
import java.util.List;

public class Ref extends CrossRefObject {
	Doc crossrefDoc;
	protected Scope scope;
	private List<IRefListener> listeners=new ArrayList<>();
	private IRefListener[] listenersAsArray;
	protected List<Obj> resolvedTo=new ArrayList<>();
	public Ref(Doc crossrefDoc, Scope scope) {
		super(crossrefDoc.getHost());
		this.crossrefDoc=crossrefDoc;
		this.scope=scope;
	}
	public Ref addListener(IRefListener rl)
	{
		listeners.add(rl);
		listenersAsArray=null;
		return this;
	}
	/**
	 * Get an onmodified copy for iteration.
	 * TODO make it faster
	 * @return
	 */
	private IRefListener[] getListenersCopy() {
		if(listenersAsArray==null)
		{
			listenersAsArray=listeners.toArray(new IRefListener[] {});
		}
		return listenersAsArray;
	}
	private void notifyListeners(List<Obj> finds) {
		for(IRefListener l:getListenersCopy())
		{
			l.resolvedTo(finds);
		}
	}
	/**
	 * Update the resolved to list and call all listeners if this has changed.
	 * (Method must be called within an update transaction)
	 * @param found
	 */
	protected void setResolvedTo(List<Obj> found) {
		if(resolvedTo!=null)
		{
			for(Obj o: resolvedTo)
			{
				o.referencesTargetingThis.remove(this);
			}
		}
		resolvedTo=found;
		if(resolvedTo!=null)
		{
			for(Obj o: resolvedTo)
			{
				o.referencesTargetingThis.add(this);
			}
		}
		notifyListeners(found);
	}
	public List<Obj> getResolvedTo() {
		return resolvedTo;
	}
	public Scope getScope() {
		return scope;
	}
	@Override
	public Doc getDoc() {
		return crossrefDoc;
	}
}
