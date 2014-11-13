package hu.qgears.coolrmi;

public class CoolRMIRemotableObjectServerSide<T> implements CoolRMIRemotableObject<T>{
	private T accessor;
	public T getAccessor()
	{
		return accessor;
	}
}
