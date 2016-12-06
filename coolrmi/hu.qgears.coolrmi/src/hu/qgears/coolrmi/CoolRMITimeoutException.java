package hu.qgears.coolrmi;

import java.util.concurrent.TimeoutException;

public class CoolRMITimeoutException extends CoolRMIException {

	public CoolRMITimeoutException(TimeoutException e1) {
		super(e1);
	}

	private static final long serialVersionUID = 1L;

}
