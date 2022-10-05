package hu.qgears.parser.editor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import hu.qgears.crossref.Obj;
import hu.qgears.emfcollab.backref.EmfReference;
import hu.qgears.xtextgrammar.CRAEObject;
import hu.qgears.xtextgrammar.CRAEReference;

public class QEdLabelProvider extends CellLabelProvider {
	@Override
	public void update(ViewerCell cell) {
		Object o=cell.getElement();
		if(o!=null)
		{
			if(o instanceof String)
			{
				cell.setText((String) o);
			}else if(o instanceof EObject)
			{
				EObject eo=(EObject) o;
				synchronized (eo.eResource().getResourceSet()) {
					StringBuilder label=new StringBuilder();
					if(eo.eContainingFeature()!=null)
					{
						label.append(eo.eContainingFeature().getName());
						label.append(": ");
					}
					label.append(getNameAndIcon(eo));
					cell.setText(label.toString());
				}
			}else if(o instanceof RefInTree)
			{
				RefInTree rit=(RefInTree) o;
				StringBuilder ret=new StringBuilder();
				ret.append(rit.r.getName());
				ret.append(": ");
				ret.append(getNameAndIcon(rit.tg));
				ret.append(" - ");
				ret.append(getNameObjectId(rit.tg));
				cell.setText(ret.toString());
			}else if(o instanceof EmfReference)
			{
				EmfReference rit=(EmfReference) o;
				StringBuilder ret=new StringBuilder();
				ret.append(""+rit+" ");
				ret.append(rit.getRefType().getName());
				ret.append(": ");
				ret.append(getNameAndIcon(rit.getTarget()));
				ret.append(" - ");
				ret.append(getNameObjectId(rit.getTarget()));
				cell.setText(ret.toString());
			}
			else if(o instanceof Resource)
			{
				Resource r=(Resource) o;
				synchronized (r.getResourceSet()) {
					cell.setText(r.getURI().toString());
				}
			}
		}
	}
	protected String getNameAndIcon(EObject eo) {
		String oid=getNameObjectId(eo);
		return ""+eo+" - "+oid;
	}
	protected String getNameObjectId(EObject eo)
	{
		CRAEObject cra=CRAEObject.getAllowNull(eo);
		if(cra!=null)
		{
			Obj o=cra.getNameObject();
			StringBuilder sb=new StringBuilder();
			if(o!=null)
			{
				sb.append(o.getFqId());
			}
			if(cra.getUnresolvedCrossReference()!=null)
			{
				CRAEReference cri=cra.getUnresolvedCrossReference();
				sb.append("REF: "+cri.unresolvedReferenceRawRecerenceString);
				if(cri.ref!=null)
				{
					sb.append(" "+cri.ref.getScope());
				}
				Object d=cri.getDebugDynamicResolve();
				if(d!=null)
				{
					sb.append(" "+d);
				}else
				{
					sb.append("NOPE");
				}
			}
			return sb.toString();
		}
		return null;
	}
}
