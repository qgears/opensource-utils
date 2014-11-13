package hu.qgears.commons;

/**
 * A wrapper class for a property that's value can be get and set and
 * listeners can be added to the setting event.
 * @author rizsi
 *
 * @param <T>
 */
public class UtilListenableProperty<T> {
	private T property;
	private UtilEvent<T> propertyChangedEvent=new UtilEvent<T>(); 
	
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
		this.property = property;
		propertyChangedEvent.eventHappened(property);
	}
}
