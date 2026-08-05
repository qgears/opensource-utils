package hu.qgears.emfcollab;

public class EmfEventSetAttribute extends AEmfEvent {
	private static final long serialVersionUID = 1L;
	private String objectId;
	private String attributeName;
	private String serializedValue;
	private String serializedOldValue;
	private String objectTypeName;
	
	public String getSerializedOldValue() {
		return serializedOldValue;
	}
	public void setSerializedOldValue(String serializedOldValue) {
		this.serializedOldValue = serializedOldValue;
	}
	public String getObjectTypeName() {
		return objectTypeName;
	}
	public void setObjectTypeName(String objectTypeName) {
		this.objectTypeName = objectTypeName;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getSerializedValue() {
		return serializedValue;
	}
	public void setSerializedValue(String serializedValue) {
		this.serializedValue = serializedValue;
	}
	@Override
	public String toString() {
		return "set: "+objectId+"."+attributeName+": "+serializedValue+" ("+objectTypeName+") oldvalue: "+serializedOldValue;
	}
	@Override
	public EmfEventType getType() {
		return EmfEventType.setAttribute;
	}
}
