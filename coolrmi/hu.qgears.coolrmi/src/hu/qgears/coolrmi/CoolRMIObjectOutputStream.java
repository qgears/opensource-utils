package hu.qgears.coolrmi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import hu.qgears.coolrmi.remoter.CoolRMIReplaceEntry;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import hu.qgears.coolrmi.remoter.ICoolRMIReplaceType;

/**
 * Object output stream with replace capability.
 * See {@link CoolRMIReplaceEntry}
 * @author rizsi
 *
 */
public class CoolRMIObjectOutputStream extends ObjectOutputStream
{
	private CoolRMIServiceRegistry reg;
	public CoolRMIObjectOutputStream(CoolRMIServiceRegistry reg, OutputStream out) throws IOException {
		super(out);
		this.reg=reg;
		enableReplaceObject(true);
	}

	@Override
	protected Object replaceObject(Object obj) throws IOException {
		if(obj instanceof ICoolRMIReplaceType)
		{
			return ((ICoolRMIReplaceType)obj).coolRMIReplaceObject();
		}
		Object ret=reg.replaceObject(obj);
		if(ret==null)
		{
			if(!(obj instanceof Serializable))
			{
				ret=reg.replaceObjectHeavy(obj);
				if(ret!=null)
				{
					return ret;
				}
			}
			return super.replaceObject(obj);
		}
		return ret;
	}
}
