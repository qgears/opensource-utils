package hu.qgears.crossref;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Ref extends CrossRefObject {
	Doc crossrefDoc;
	protected Scope scope;
	private List<IRefListener> listeners=new ArrayList<>();
	private IRefListener[] listenersAsArray;
	protected List<Obj> resolvedTo=new ArrayList<>();
	protected Ref(Doc crossrefDoc, Scope scope) {
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
	public void removeListener(IRefListener rl)
	{
		listeners.remove(rl);
		listenersAsArray=null;
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
			l.resolvedTo(this,finds);
		}
	}
	/**
	 * Update the resolved to list and call all listeners if this has changed.
	 * (Method must be called within an update transaction)
	 * @param found
	 */
	protected void setResolvedTo(Set<Obj> found) {
		if(resolvedTo!=null)
		{
			for(Obj o: resolvedTo)
			{
				o.referencesTargetingThis.remove(this);
			}
		}
		resolvedTo=found==null?null:new ArrayList<>(found);
		if(resolvedTo!=null)
		{
			for(Obj o: resolvedTo)
			{
				o.referencesTargetingThis.add(this);
			}
		}
		notifyListeners(resolvedTo);
		crossrefDoc.refResolvedChanged(this);
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
