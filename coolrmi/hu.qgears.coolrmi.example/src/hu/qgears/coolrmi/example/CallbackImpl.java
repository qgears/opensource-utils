package hu.qgears.coolrmi.example;

import java.io.IOException;

import hu.qgears.coolrmi.CoolRMIClient;
import hu.qgears.coolrmi.ICoolRMIProxy;

/**
 * Callback implementation on the client side.
 * 
 * This object is sent as a proxy object so that the server can call its methods
 * through the CoolRMI connection. See {@link RemotingConfiguration} how it is registered.
 * 
 * @author rizsi
 *
 */
public class CallbackImpl implements ICallback
{
	private CoolRMIClient c;
	private IService service;
	public CallbackImpl(CoolRMIClient c, IService s) {
		this.c=c;
		this.service=s;
	}

	@Override
	public void callback(String s) {
		System.out.println("Callback returned: "+s);
		System.out.println("We close the CoolRMI service and then the application exits.");
		try {
			((ICoolRMIProxy) service).disposeProxy();
			c.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
