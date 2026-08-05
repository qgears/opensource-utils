package hu.qgears.emfcollab;

/**
 * Event that represents the moving of an EMF object within
 * a relation to a new position index.
 * @author rizsi
 *
 */
public class EmfEventMove extends AEmfEvent {
	private static final long serialVersionUID = 1L;
	String parentId;
	String referenceName;
	String movedId;
	int position;
	int oldPosition;
	
	public int getOldPosition() {
		return oldPosition;
	}
	public void setOldPosition(int oldPosition) {
		this.oldPosition = oldPosition;
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
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getMovedId() {
		return movedId;
	}
	public void setMovedId(String movedId) {
		this.movedId = movedId;
	}
	@Override
	public String toString() {
		return "move: "+parentId+"."+referenceName+" "+position+" "+movedId;
	}
	@Override
	public EmfEventType getType() {
		return EmfEventType.move;
	}
}
