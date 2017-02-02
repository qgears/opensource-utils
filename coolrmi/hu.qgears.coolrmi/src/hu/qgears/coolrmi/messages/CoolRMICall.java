/**
 *  Copyright CoolRMI Schmidt András

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package hu.qgears.coolrmi.messages;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import hu.qgears.commons.signal.SignalFuture;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.commons.signal.Slot;
import hu.qgears.coolrmi.CoolRMIException;
import hu.qgears.coolrmi.CoolRMIReplyHandler;
import hu.qgears.coolrmi.remoter.CoolRMIServerSideObject;
import hu.qgears.coolrmi.remoter.GenericCoolRMIRemoter;


/**
 * The message sent over network for a method call request.
 * @author rizsi
 *
 */
public class CoolRMICall
	extends AbstractCoolRMICall
	implements Serializable{
	private static final ThreadLocal<CoolRMICall> currentCall=new ThreadLocal<CoolRMICall>();
	private static final long serialVersionUID = 1L;
	private transient GenericCoolRMIRemoter remoter;
	private transient CoolRMIReplyAsync asyncReply;
	private transient CoolRMIReplyHandler nextCallAsync;
	private String method;
	private Object[] args;
	private long proxyId;
	private boolean stopOnException;
	public CoolRMICall(long callId, long proxyId, String method, Object[] args, boolean stopOnException) {
		super(callId);
		this.proxyId = proxyId;
		this.method = method;
		this.args = args;
		this.stopOnException=stopOnException;
	}
	public long getProxyId() {
		return proxyId;
	}
	public String getMethod() {
		return method;
	}
	public Object[] getArgs() {
		return args;
	}
	@Override
	public String toString() {
		return "CoolRMICall: "+getQueryId()+" proxy: "+proxyId+"."+method;
	}
	@Override
	public void executeServerSide(final GenericCoolRMIRemoter coolRMIRemoter) throws IOException {
		coolRMIRemoter.execute(new Runnable() {
			@Override
			public void run() {
				try {
					CoolRMIReply reply=executeOnExecutorThread(coolRMIRemoter);
					if(!reply.isAsync())
					{
						coolRMIRemoter.send(reply);
					}
				} catch (Exception e) {
					// We can not do anything clever here.
					e.printStackTrace();
				}
			}
		});
	}
	public CoolRMIReply executeOnExecutorThread(final GenericCoolRMIRemoter coolRMIRemoter) throws ClassNotFoundException
	{
		remoter=coolRMIRemoter;
		final long callId = getQueryId();
		CoolRMIServerSideObject proxy = coolRMIRemoter.getProxyById(getProxyId());
		if (proxy == null) {
			CoolRMIReply reply = new CoolRMIReply(callId, null,
					new CoolRMIException("Server side proxy does not exist"));
			return reply;
		} else {
			final Object service = proxy.getService();
			Class<?> clazz = service.getClass();
			final String reqMethod=getMethod();
			Method[] methods = clazz.getMethods();
			for (final Method m : methods) {
				if (reqMethod.equals(m.getName())) {
					final Object[] args=coolRMIRemoter.resolveProxyInParamersClientSide(getArgs());
					try {
						registerCurrentCall();
						Object ret = m.invoke(service, args);
						ret=coolRMIRemoter.resolveProxyInParamerServerSide(ret);
						if(asyncReply!=null)
						{
							return asyncReply;
						}else
						{
							CoolRMIReply reply = new CoolRMIReply(callId,
									ret, null);
							return reply;
						}
					} catch (InvocationTargetException exc) {
						return new CoolRMIReply(callId, null, exc
								.getCause());
					} catch (Throwable t) {
						System.err.println("Err method: "+reqMethod);
						return new CoolRMIReply(callId, null, t);
					} finally
					{
						unregisterCurrentCall();
					}
				}
			}
			// method not found
			CoolRMIReply reply = new CoolRMIReply(callId, null,
					new CoolRMIException("No such method on service: "
							+ proxy.getService() + " (callid " + callId + ") "
							+ getMethod()));
			return reply;
		}
	}
	public boolean isStopOnException()
	{
		return stopOnException;
	}
	/**
	 * Get accessor to the CoolRMI system where asynchronous call 
	 * or asynchronous return can be set up.
	 * Never returns null.
	 * @return
	 */
	public static CoolRMICall getCurrentCall()
	{
		CoolRMICall ret=currentCall.get();
		if(ret==null)
		{
			ret=new CoolRMICall(-1, -1, "", null, false);
			currentCall.set(ret);
		}
		return ret;
	}

	public void registerCurrentCall() {
		currentCall.set(this);
	}

	public void unregisterCurrentCall() {
		currentCall.set(null);
	}
	public GenericCoolRMIRemoter getRemoter() {
		return remoter;
	}
	public CoolRMIReplyAsync createAsyncReply() {
		asyncReply=new CoolRMIReplyAsync(remoter, getQueryId());
		return asyncReply;
	}
	public void createAsyncReply(SignalFutureWrapper<Object> ret) {
		asyncReply=new CoolRMIReplyAsync(remoter, getQueryId());
		ret.addOnReadyHandler(new Slot<SignalFuture<Object>>() {
			
			@Override
			public void signal(SignalFuture<Object> value) {
				asyncReply.reply(value.getSimple(), value.getThrowable());
			}
		});
	}
	/**
	 * Mark that the next method call is asynchronous.
	 * @param handler The reply calls this handler. May be null. Null means that the call is asynchronous but the reply is not listened.
	 */
	public void asyncCall(CoolRMIReplyHandler handler) {
		if(handler==null)
		{
			nextCallAsync=new CoolRMIReplyHandler();
		}else
		{
			nextCallAsync=handler;
		}
	}
	public CoolRMIReplyHandler removeCurrentAsynCall() {
		CoolRMIReplyHandler ret=nextCallAsync;
		nextCallAsync=null;
		return ret;
	}
}
