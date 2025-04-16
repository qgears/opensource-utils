package hu.qgears.coolrmi.multiplexer;

import hu.qgears.coolrmi.messages.AbstractCoolRMIMessage;

public interface ISocketMultiplexer {

	void addMessageToSend(byte[] bs, AbstractCoolRMIMessage message);

	void stop();

}
