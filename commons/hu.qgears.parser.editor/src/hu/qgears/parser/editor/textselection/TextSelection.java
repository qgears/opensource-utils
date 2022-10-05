package hu.qgears.parser.editor.textselection;

import org.eclipse.emf.ecore.EObject;

import hu.qgears.parser.editor.RefInTree;
import hu.qgears.xtextgrammar.SourceReference;

public class TextSelection implements Comparable<TextSelection>{
	public static boolean isCaretInside(SourceReference sr, int caretOffset) {
		if(sr==null)
		{
			return false;
		}
		return sr.getTextIndexFrom()<=caretOffset && sr.getTextIndexTo()>caretOffset;
	}
	protected int length;
	@SuppressWarnings("unused")
	private SourceReference sr;
	private EObject eo;
	private RefInTree ref;
	public TextSelection(EObject eo, SourceReference sr) {
		super();
		this.eo=eo;
		this.sr=sr;
		length=sr.getLength();
	}
	public TextSelection(RefInTree ref, SourceReference sr) {
		this.ref=ref;
		this.eo=ref.host;
		length=sr.getLength();
	}
	public EObject getTarget() {
		return eo;
	}
	@Override
	public int compareTo(TextSelection o) {
		return length-o.length;
	}
	public RefInTree getRef() {
		return ref;
	}
	public int getLength() {
		return length;
	}
}
