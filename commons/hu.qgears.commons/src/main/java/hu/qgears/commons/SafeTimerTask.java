package hu.qgears.commons;

import java.util.TimerTask;

//TODO it is just a mock, please delete me an commit the valid version
public abstract class SafeTimerTask extends TimerTask {

	@Override
	public void run() {
		doRun();
	}

	protected abstract void doRun();

}
