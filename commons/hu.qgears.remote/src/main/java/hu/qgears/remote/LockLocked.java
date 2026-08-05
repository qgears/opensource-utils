package hu.qgears.remote;

import hu.qgears.coolrmi.ICoolRMIDisposable;

public class LockLocked implements ILockLocked, ICoolRMIDisposable {
	private RemoteIf remoteIf;
	public LockLocked(RemoteIf remoteIf) {
		this.remoteIf=remoteIf;
	}
	@Override
	public void disposeWhenDisconnected() {
		System.out.println("Lock owner Disconnected");
		unlock();
	}
	@Override
	public void unlock() {
		System.out.println("Unlock lock!");
		synchronized (this) {
			if(remoteIf!=null)
			{
				remoteIf.unlocked(this);
				remoteIf=null;
			}
		}
	}
}
