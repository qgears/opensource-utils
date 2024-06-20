package hu.qgears.remote;

import java.io.IOException;
import java.io.OutputStream;

import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.coolrmi.ICoolRMIDisposable;

public class CallbackImpl implements ICallback, ICoolRMIDisposable {
	private OutputStream os;
	private boolean closeAllowed=true;
	public final SignalFutureWrapper<Boolean> closed=new SignalFutureWrapper<>();
	public CallbackImpl(OutputStream os) {
		super();
		this.os = os;
	}
	@Override
	public void close() {
		try {
			try {
				os.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			closed.ready(true, null);
			if(closeAllowed)
			{
				os.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void data(byte[] copyOf) {
		try {
			os.write(copyOf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public CallbackImpl setCloseAllowed(boolean closeAllowed) {
		this.closeAllowed = closeAllowed;
		return this;
	}
	@Override
	public void disposeWhenDisconnected() {
		close();
	}
}
