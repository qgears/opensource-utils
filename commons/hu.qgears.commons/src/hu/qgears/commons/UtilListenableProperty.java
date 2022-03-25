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
	private T property;
	private final UtilEvent<T> propertyChangedEvent=new UtilEvent<T>(); 
	
	public UtilListenableProperty() {
		super();
	}

	public UtilListenableProperty(T property) {
		super();
		this.property = property;
	}

	public UtilEvent<T> getPropertyChangedEvent() {
		return propertyChangedEvent;
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
		propertyChangedEvent.eventHappened(property);
	}
}
