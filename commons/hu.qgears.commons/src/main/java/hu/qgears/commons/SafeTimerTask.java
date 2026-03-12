package hu.qgears.commons;

import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * Timer task with exception wrapping.
 * Using the Timer/TimerTask API if a timer
 * throws an exception then the timer dies. This is a problem when
 * multiple functions share a single timer.
 */
abstract public class SafeTimerTask extends TimerTask {
	private static Logger safeTimerTastLog=Logger.getLogger(SafeTimerTask.class);
	private volatile boolean cancelled=false;
	@Override
	final public void run() {
		try {
			if(!cancelled)
			{
				doRun();
			}
		} catch (Exception e) {
			safeTimerTastLog.error("Uncaught exception in timer", e);
		}
	}
	abstract protected void doRun();
	@Override
	public boolean cancel() {
		cancelled=true;
		return super.cancel();
	}
	public boolean isCancelled()
	{
		return cancelled;
	}
}
