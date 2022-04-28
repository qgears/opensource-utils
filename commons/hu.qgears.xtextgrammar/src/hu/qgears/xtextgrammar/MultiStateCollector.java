package hu.qgears.xtextgrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;
import hu.qgears.commons.UtilString;

public class MultiStateCollector extends UtilListenableProperty<Boolean> {
	private class Sub implements UtilEventListener<Boolean> {
		private boolean disposed=false;
		public Sub(UtilListenableProperty<Boolean> prop) {
			prop.getPropertyChangedEvent().addListener(this);
		}
		@Override
		public void eventHappened(Boolean msg) {
			synchronized (subs) {
				if(!disposed)
				{
					if(!msg)
					{
						missing.add(this);
					}else
					{
						missing.remove(this);
					}
				}
			}
			checkOk();
		}
		private void dispose(UtilListenableProperty<Boolean> prop)
		{
			prop.getPropertyChangedEvent().removeListener(this);
			synchronized (subs) {
				disposed=true;
				missing.remove(this);
			}
		}
	}
	private Map<UtilListenableProperty<Boolean>, Sub> subs=new HashMap<UtilListenableProperty<Boolean>, Sub>();
	private Set<Sub> missing=new HashSet<>();
	public MultiStateCollector() {
		super(true);
	}
	public void addSubProperty(UtilListenableProperty<Boolean> prop)
	{
		synchronized (subs) {
			Sub s=subs.get(prop);
			if(s==null)
			{
				s=new Sub(prop);
				subs.put(prop, s);
				prop.getPropertyChangedEvent().addListener(s);
				if(!prop.getProperty())
				{
					missing.add(s);
				}
			}
		}
		checkOk();
	}
	public void removeSubProperty(UtilListenableProperty<Boolean> prop)
	{
		synchronized (subs) {
			Sub s=subs.remove(prop);
			if(s!=null)
			{
				s.dispose(prop);
			}
		}
		checkOk();
	}
	private void checkOk() {
		synchronized (subs) {
			setProperty(missing.isEmpty());
		}
	}
	public String debugString() {
		return UtilString.concatGenericList(new ArrayList<>(subs.keySet()), ", ");
	}
}
