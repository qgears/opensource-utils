package hu.qgears.commons;

/**
 * A wrapper class for a property that's value can be get and set and
 * listeners can be added to the setting event.
 * 
 * In case the value is set to an other object that is equal to the current one then the event is not propagated.
 *
 * @param <T>
 */
public class UtilListenableProperty<T> {
	private String name;
	private T property;
	private UtilEvent<T> propertyChangedEvent=null; 
	
	public UtilListenableProperty() {
		super();
	}

	public UtilListenableProperty(T property) {
		super();
		this.property = property;
	}

	public UtilEvent<T> getPropertyChangedEvent() {
		synchronized (this) {
			if(propertyChangedEvent==null)
			{
				propertyChangedEvent=new UtilEvent<>();
			}
			return propertyChangedEvent;
		}
	}
	public UtilEvent<T> getPropertyChangedEventOrNull() {
		synchronized (this) {
			return propertyChangedEvent;
		}
	}

	public T getProperty() {
		return property;
	}

	public void setProperty(T property) {
		if(property==null && this.property==null) // NOSONAR  pmd:BrokenNullCheck false positive
		{
			return;
		}
		if(property!=null && property.equals(this.property))
		{
			this.property = property;
			return;
		}
		this.property = property;
		UtilEvent<T> propertyChangedEvent=getPropertyChangedEventOrNull();
		if(propertyChangedEvent!=null)
		{
			propertyChangedEvent.eventHappened(property);
		}
	}
	/**
	 * Add a listener and execute listener with the current value of the property.
	 * @param l
	 * @return closeable object that removes the listener when closed
	 */
	public NoExceptionAutoClosable addListenerWithInitialTrigger(UtilEventListener<T> l)
	{
		getPropertyChangedEvent().addListener(l);
		l.eventHappened(getProperty());
		return new NoExceptionAutoClosable() {
			@Override
			public void close() {
				getPropertyChangedEvent().removeListener(l);
			}
		};
	}
	public void setName(String name) {
		this.name=name;
	}
	@Override
	public String toString() {
		return ""+name+": "+property;
	}
	/**
	 * Clone an ohter property object by copying that's current value
	 * and tracking its value by adding a listener to that.
	 * @param toClone
	 * @return Closable object that when closed then the listener is removed
	 */
	public NoExceptionAutoClosable cloneProperty(UtilListenableProperty<T> toClone)
	{
		UtilEventListener<T> l=s->setProperty(s);
		toClone.getPropertyChangedEvent().addListener(l);
		setProperty(toClone.getProperty());
		return new NoExceptionAutoClosable() {
			public void close() {
				toClone.getPropertyChangedEvent().removeListener(l);
			};
		};
	}
}
