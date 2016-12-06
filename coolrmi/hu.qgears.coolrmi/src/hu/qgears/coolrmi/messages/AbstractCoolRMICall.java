package hu.qgears.coolrmi.messages;

import java.io.IOException;
import java.util.concurrent.Executor;

import hu.qgears.coolrmi.remoter.CoolRMIRemoter;

abstract public class AbstractCoolRMICall extends AbstractCoolRMIMessage
{
	private static final long serialVersionUID = 1L;

	public AbstractCoolRMICall() {
		super();
	}

	public AbstractCoolRMICall(long queryId) {
		super(queryId);
	}

	abstract public void executeServerSide(CoolRMIRemoter coolRMIRemoter, Executor serverSideExecutor) throws IOException;
	@Override
	public String getName() {
		return "RMI call";
	}
}
