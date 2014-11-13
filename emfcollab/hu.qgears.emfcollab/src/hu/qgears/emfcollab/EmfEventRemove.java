package hu.qgears.emfcollab;

/**
 * Event that represents the removal of an EMF object from a reference.
 * @author rizsi
 *
 */
public class EmfEventRemove extends AEmfEvent {
	private static final long serialVersionUID = 1L;
	private String sourceId;
	private String referenceName;
	private String removedId;
	private int position;
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getRemovedId() {
		return removedId;
	}
	public void setRemovedId(String removedId) {
		this.removedId = removedId;
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
		return "remove: "+removedId+" from "+sourceId+"."+referenceName;
	}
	@Override
	public EmfEventType getType() {
		return EmfEventType.remove;
	}
}
