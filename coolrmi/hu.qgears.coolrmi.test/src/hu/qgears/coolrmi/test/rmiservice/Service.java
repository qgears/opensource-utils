package hu.qgears.coolrmi.test.rmiservice;

import java.nio.IntBuffer;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import hu.qgears.commons.UtilTimer;
import hu.qgears.coolrmi.ICoolRMIProxy;

public class Service implements IService
{
	private volatile int nCall;
	@Override
	public String echo(String s, int x) {
		nCall++;
		StringBuilder ret=new StringBuilder();
		for(int i=0;i<x;++i)
		{
			ret.append(s);
			ret.append("\n");
		}
		return ret.toString();
	}
	@Override
	public void initTimer(ICallback cb, long timeoutMs) {
		nCall++;
		UtilTimer.getInstance().executeTimeout(timeoutMs, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				cb.callback("Millis elapsed: "+timeoutMs);
				// Cool RMI proxy objects must be disposed when not used any more.
				// The implementation allows local usage without remoting as well.
				if(cb instanceof ICoolRMIProxy)
				{
					((ICoolRMIProxy)cb).disposeProxy();
				}
				return null;
			}
		});
	}
	@Override
	public String nonSerializableArgument(IntBuffer ib) {
		nCall++;
		return "First value in buffer: "+ib.get();
	}
	@Override
	public void exceptionExample() throws RemoteException {
		nCall++;
		throw new RemoteException("This is thrown by the server. Stack traces on the server and the client are merged.");
	}
	public int getnCall() {
		return nCall;
	}
}
