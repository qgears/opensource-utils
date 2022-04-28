package hu.qgears.parser.editor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class RefInTree {
	public final EObject host;
	public final EReference r;
	public final EObject tg;
	public RefInTree(EObject host, EReference r, EObject tg){
		this.host=host;
		this.r=r;
		this.tg=tg;
	}
}
