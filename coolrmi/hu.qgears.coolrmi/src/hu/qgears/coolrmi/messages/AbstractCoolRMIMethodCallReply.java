package hu.qgears.coolrmi.messages;

import hu.qgears.coolrmi.remoter.CoolRMIProxy;

abstract public class AbstractCoolRMIMethodCallReply extends AbstractCoolRMIReply
{
	private static final long serialVersionUID = 1L;

	public AbstractCoolRMIMethodCallReply() {
		super();
	}

	public AbstractCoolRMIMethodCallReply(long queryId) {
		super(queryId);
	}

	/**
	 * 
	 * @param coolRMIProxy 
	 * @param remoter
	 * @param returnLast when true then return the value returned by last method call.
	 * @return
	 * @throws Throwable
	 */
	abstract public Object evaluateOnClientSide(CoolRMIProxy coolRMIProxy, boolean returnLast) throws Throwable;
}
