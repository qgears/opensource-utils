package hu.qgears.commons;

import java.util.TimerTask;

import org.apache.log4j.Logger;

public abstract class SafeTimerTask extends TimerTask {

	private static final Logger LOG = Logger.getLogger(SafeTimerTask.class);
	
	@Override
	public void run() {
		try {
			doRun();
		} catch (Exception e) {
			LOG.error("Timer task failed",e);
		}
	}

	protected abstract void doRun();

}
