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

import hu.qgears.coolrmi.remoter.CoolRMIProxy;
import hu.qgears.coolrmi.remoter.CoolRMIRemoter;


/**
 * The message sent over network as reply to a method call request.
 * The object may contain an exception or a return value.
 * @author rizsi
 *
 */
public class CoolRMIReply extends AbstractCoolRMIMethodCallReply
{
	private static final long serialVersionUID = 1L;
	private Throwable exception;
	public Throwable getException() {
		return exception;
	}
	public Object getRet() {
		return ret;
	}
	private Object ret;
	public CoolRMIReply(long callId, Object ret, Throwable exception) {
		super(callId);
		this.ret = ret;
		this.exception = exception;
	}
	@Override
	public String toString() {
		return "CoolRMIReply: "+getQueryId();
	}
	@Override
	public Object evaluateOnClientSide(CoolRMIProxy coolRMIProxy, boolean returnLast) throws Throwable {
		resolveArgumentsOnClient(coolRMIProxy.getRemoter());
		if (getException() == null) {
			return ret;
		}else
		{
			throw getException();
		}
	}
	public void resolveArgumentsOnClient(CoolRMIRemoter remoter)
	{
		if (getException() == null) {
			ret=remoter.resolveProxyInParamerClientSide(getRet());
		}else
		{
			StackTraceElement[] stprev=getException().getStackTrace();
			StackTraceElement[] toadd=Thread.currentThread().getStackTrace();
			StackTraceElement[] merged=new StackTraceElement[stprev.length+toadd.length];
			int i=0;
			for(StackTraceElement e:stprev)
			{
				merged[i++]=e;
			}
			for(StackTraceElement e:toadd)
			{
				merged[i++]=e;
			}
			getException().setStackTrace(merged);
		}
	}
}
