package hu.qgears.coolrmi.messages;

import java.io.IOException;

import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;

abstract public class AbstractCoolRMICall extends AbstractCoolRMIMessage
{
	private static final long serialVersionUID = 1L;

	public AbstractCoolRMICall() {
		super();
	}

	public AbstractCoolRMICall(long queryId) {
		super(queryId);
	}

	abstract public void executeServerSide(GenericCoolRMIRemoter coolRMIRemoter) throws IOException;
	@Override
	public String getName() {
		return "RMI call";
	}
}
