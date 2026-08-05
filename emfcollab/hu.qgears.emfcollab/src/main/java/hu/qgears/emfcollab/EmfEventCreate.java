package hu.qgears.emfcollab;

/**
 * Event that represents the creation of an EMF object.
 * @author rizsi
 *
 */
public class EmfEventCreate extends AEmfEvent {
	private static final long serialVersionUID = 1L;
	String parentId;
	String referenceName;
	String createdId;
	String createdType;
	int position;
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getReferenceName() {
		return referenceName;
	}
	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}
	public String getCreatedId() {
		return createdId;
	}
	public void setCreatedId(String createdId) {
		this.createdId = createdId;
	}
	public String getCreatedType() {
		return createdType;
	}
	public void setCreatedType(String createdType) {
		this.createdType = createdType;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
	public String toString() {
		return "create: "+parentId+"."+referenceName+" "+createdType;
	}
	@Override
	public EmfEventType getType() {
		return EmfEventType.create;
	}
}
