package hu.qgears.coolrmi.messages;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.TimeUnit;

import hu.qgears.commons.signal.SignalFutureWrapper;

public class CoolRMIDisconnect extends AbstractCoolRMIMessage implements Externalizable
{
	private static final long serialVersionUID = 1L;
	private SignalFutureWrapper<Object> sent=new SignalFutureWrapper<Object>();

	@Override
	public String getName() {
		return "Disconnect";
	}

	public void waitSent(long timeoutMillis) {
		try {
			sent.get(timeoutMillis, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			// Best effort to gracefully close the channel, exception is not logged.
		}
	}
	@Override
	public void sent() {
		sent.ready("", null);
		super.sent();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}
}
