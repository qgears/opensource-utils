package hu.qgears.crossref;

import java.util.function.Function;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilListenableProperty;

/**
 * Listen to event to find event to listen to find event to listen to find the target.
 * Do all this in a manner that just work and does not leak memory.
 */
public class ChainedUtilEvent <T, U> implements NoExceptionAutoClosable {
	private UtilListenableProperty<T> a;
	Function<T,UtilListenableProperty<U>> fb;
	private UtilListenableProperty<U> b;
	public final UtilListenableProperty<U> output=new UtilListenableProperty<>();
	private UtilEventListener<U> liU=new UtilEventListener<U>() {
		public void eventHappened(U msg) {output.setProperty(msg);};
	};
	private UtilEventListener<T> liT=new UtilEventListener<T>(){
		@Override
		public void eventHappened(T msg) {
			if(msg!=null)
			{
				UtilListenableProperty<U> u=fb.apply(msg);
				if(u!=b)
				{
					removeBListener();
					if(u!=null)
					{
						u.addListenerWithInitialTrigger(liU);
					}
				}
			}
		}
	};
	public ChainedUtilEvent(UtilListenableProperty<T> a, Function<T,UtilListenableProperty<U>> fb) {
		this.a=a;
		this.fb=fb;
		a.addListenerWithInitialTrigger(liT);
	}
	private void removeBListener() {
		if(b!=null)
		{
			b.getPropertyChangedEvent().removeListener(liU);
			b=null;
		}
	}
	@Override
	public void close() {
		a.getPropertyChangedEvent().removeListener(liT);
		removeBListener();
		output.setProperty(null);
	}
}
