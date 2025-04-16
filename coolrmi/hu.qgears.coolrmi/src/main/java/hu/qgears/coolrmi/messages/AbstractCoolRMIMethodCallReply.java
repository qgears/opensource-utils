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
	 * This method must be called to process the method call reply:
	 * 
	 *  * in case of method call list call the callbacks of method call replies.
	 *  * process proxied objects to transform them to their client side representation.
	 *  * process returned stack traces: add the server side stack trace to them.
	 *  
	 * @param coolRMIProxy 
	 * @param remoter
	 * @param returnLast when true then return the value returned by last method call.
	 * @throws ClassNotFoundException 
	 */
	abstract public void evaluateOnClientSide(CoolRMIProxy coolRMIProxy, boolean returnLast) throws ClassNotFoundException;
	/**
	 * The exception thrown on the server when executing the method.
	 * @return
	 */
	abstract public Throwable getException();
	/**
	 * The return value of the method execution on the server. Only valid in case the exception is null.
	 * @return
	 */
	abstract public Object getRet();
}
