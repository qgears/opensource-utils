package hu.qgears.parser.editor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class RefInTree {
	public final EObject host;
	public final EReference r;
	public final EObject tg;
	private int hashCode;
	public RefInTree(EObject host, EReference r, EObject tg){
		this.host=host;
		this.r=r;
		this.tg=tg;
		hashCode=hc(host)^hc(r)^hc(tg);
	}
	private int hc(Object o) {
		return o==null?0:o.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RefInTree)
		{
			RefInTree other=(RefInTree) obj;
			return other.tg==tg && other.r==r && other.host==host;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return hashCode;
	}
	public static RefInTree create(EObject element, EReference r2, EObject target, int index) {
		// TODO Auto-generated method stub
		return null;
	}
}
