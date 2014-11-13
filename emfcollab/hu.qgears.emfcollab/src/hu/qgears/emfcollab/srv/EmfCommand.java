package hu.qgears.emfcollab.srv;

import hu.qgears.emfcollab.EmfEvent;

import java.util.List;


/**
 * Serializable Emf Command representation.
 * 
 * Can be executed by EmfCommandExecutor
 * @author rizsi
 *
 */
public class EmfCommand implements EmfSerializable {
	private static final long serialVersionUID = 1L;
	private EmfSessionId owner;
	private long commandIndex;
	private String name;
	private List<EmfEvent> events;
	public EmfCommand(long commandIndex, String name,
			List<EmfEvent> events, EmfSessionId owner) {
		super();
		this.commandIndex = commandIndex;
		this.name = name;
		this.events = events;
		this.owner=owner;
	}
	public long getCommandIndex() {
		return commandIndex;
	}
	public String getName() {
		return name;
	}
	public List<EmfEvent> getEvents() {
		return events;
	}
	public EmfSessionId getOwner() {
		return owner;
	}
	public void setOwner(EmfSessionId owner) {
		this.owner = owner;
	}
	@Override
	public String toString() {
		EmfSessionId owner=getOwner();
		return "EMF Command: "+(owner==null?"":getOwner().getUserName())+" "+getName();
	}
}
