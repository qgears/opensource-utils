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

import java.io.Serializable;


/**
 * The message sent over network for a method call request.
 * @author rizsi
 *
 */
public class CoolRMICall
	extends AbstractCoolRMIMessage
	implements Serializable{
	private static final long serialVersionUID = 1L;
	private long proxyId;
	private String method;
	private Object[] args;
	public CoolRMICall(long callId, long proxyId, String method, Object[] args) {
		super(callId);
		this.proxyId = proxyId;
		this.method = method;
		this.args = args;
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
}
