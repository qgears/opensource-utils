package hu.qgears.emfcollab;

/**
 * Event that represents the creation of an EMF object.
 * @author rizsi
 *
 */
public class EmfEventDelete extends AEmfEvent {
	private static final long serialVersionUID = 1L;

	private String sourceId;
	private String deletedType;
	private String deletedId;
	private String referenceName;
	private int removedPosition;
	
	public String getDeletedId() {
		return deletedId;
	}
	public void setDeletedId(String deletedId) {
		this.deletedId = deletedId;
	}
	public String getDeletedType() {
		return deletedType;
	}
	public void setDeletedType(String deletedType) {
		this.deletedType = deletedType;
	}
	public int getRemovedPosition() {
		return removedPosition;
	}
	public void setRemovedPosition(int removedPosition) {
		this.removedPosition = removedPosition;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getReferenceName() {
		return referenceName;
	}
	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}
	@Override
	public String toString() {
		return "delete: "+removedPosition+" from "+sourceId+"."+referenceName+" deleted id: "+deletedId;
	}
	@Override
	public EmfEventType getType() {
		return EmfEventType.delete;
	}
}
