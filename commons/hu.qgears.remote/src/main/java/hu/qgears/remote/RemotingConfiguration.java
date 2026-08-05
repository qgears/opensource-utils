package hu.qgears.remote;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;

public class RemotingConfiguration {
	/**
	 * Create a remoting configuration that supports a callback type
	 * and a serialization replace type.
	 * If we didn't use these features the configuration could be omitted.
	 * @return
	 */
	public static CoolRMIServiceRegistry createConfiguration() {
		CoolRMIServiceRegistry reg=new CoolRMIServiceRegistry();
		reg.addProxyType(CallbackImpl.class, ICallback.class);
		reg.addProxyType(ProcessCallbackImpl.class, IProcessCallback.class);
		reg.addProxyType(RemoteFileHost.class, IRemoteFile.class);
		reg.addProxyType(FolderUpdateProcess.class, IFolderUpdateProcess.class);
		reg.addProxyType(LockLocked.class, ILockLocked.class);
		return reg;
	}
}
