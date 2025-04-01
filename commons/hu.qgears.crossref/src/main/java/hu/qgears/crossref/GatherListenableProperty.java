package hu.qgears.crossref;

import java.util.function.Supplier;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;

public class GatherListenableProperty<T> extends UtilListenableProperty<T> implements NoExceptionAutoClosable {
	private UtilEvent<?> toUpdateOnEvent;
	private Supplier<T> sup;
	@SuppressWarnings("rawtypes")
	private UtilEventListener l=new UtilEventListener() {
		public void eventHappened(Object msg) {
			setProperty(sup.get());
		};
	};
	@SuppressWarnings("unchecked")
	public GatherListenableProperty(UtilEvent<?> toUpdateOnEvent, Supplier<T> sup)
	{
		this.toUpdateOnEvent=toUpdateOnEvent;
		this.sup=sup;
		toUpdateOnEvent.addListener(l);
		setProperty(sup.get());
	}
	@SuppressWarnings("unchecked")
	@Override
	public void close() {
		toUpdateOnEvent.removeListener(l);
	}
}
