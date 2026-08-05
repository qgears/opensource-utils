package hu.qgears.emfcollab;

/**
 * Event that represents the setting of a non containment reference.
 * @author rizsi
 *
 */
public class EmfEventSetReference extends AEmfEvent {
	private static final long serialVersionUID = 1L;
	String parentId;
	String referenceName;
	String createdId;
	String oldId;
	int position;
	public String getOldId() {
		return oldId;
	}
	public void setOldId(String oldId) {
		this.oldId = oldId;
	}
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
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
	public String toString() {
		return "ref: "+parentId+"."+referenceName+" "+createdId;
	}
	@Override
	public EmfEventType getType() {
		return EmfEventType.setReference;
	}
}
