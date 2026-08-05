package hu.qgears.remote;

import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.coolrmi.ICoolRMIDisposable;

public class ProcessCallbackImpl extends CallbackImpl implements IProcessCallback, ICoolRMIDisposable {
	private Process p;
	public ProcessCallbackImpl(OutputStream os, Process p) {
		super(os);
		this.p=p;
	}
	@Override
	public void destroy() {
		if(p!=null)
		{
			p.destroy();
		}
	}
	@Override
	public int exitValue(long timeoutMillis) throws InterruptedException {
		p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
		return p.exitValue();
	}
	/**
	 * Wait until task is finished. Return when either happens:
	 *  * task is marked as returned, out and err are closed
	 *  * timeout
	 * @param i
	 * @param cb
	 * @param out
	 * @param err
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	public static int waitFinish(IProcessCallback pc, int timeoutMillis, CallbackImpl out, CallbackImpl err) throws TimeoutException, ExecutionException {
		long t0Nano=System.nanoTime();
		long timeoutAtNano=t0Nano+1000l*1000l*timeoutMillis;
		int exitValue=-1;
		while(true)
		{
			if(timeoutAtNano<System.nanoTime())
			{
				throw new TimeoutException("Wait for remote process exit timeout");
			}
			try
			{
				exitValue=pc.exitValue(1000);
				long remainingNano=timeoutAtNano-System.nanoTime();
				if(remainingNano<0)
				{
					throw new TimeoutException("Wait for remote process exit timeout");
				}
				out.closed.get(remainingNano, TimeUnit.NANOSECONDS);
				remainingNano=timeoutAtNano-System.nanoTime();
				if(remainingNano<0)
				{
					throw new TimeoutException("Wait for remote process exit timeout");
				}
				err.closed.get(remainingNano, TimeUnit.NANOSECONDS);
				return exitValue;
			}catch(IllegalThreadStateException e)
			{
				// Process not finished yet - nothing to do
			}catch(InterruptedException e)
			{
				// Ignore
			}
		}
	}
	@Override
	public void disposeWhenDisconnected() {
		p.destroyForcibly();
		super.disposeWhenDisconnected();
	}
}
