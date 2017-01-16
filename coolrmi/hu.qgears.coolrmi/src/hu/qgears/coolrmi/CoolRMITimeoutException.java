package hu.qgears.coolrmi;

import java.util.concurrent.TimeoutException;

public class CoolRMITimeoutException extends CoolRMIException {

	public CoolRMITimeoutException(String message, TimeoutException e1) {
		super(message, e1);
	}

	private static final long serialVersionUID = 1L;

}
