package hu.qgears.xtextgrammar;

import hu.qgears.crossref.Doc;
import hu.qgears.parser.ITreeElem;

/**
 * Refer to the source code that created this 
 * EMF object or reference.
 */
public class SourceReference {
	private Doc doc;
	private int textIndexFrom;
	private int textIndexTo;
	public SourceReference(Doc doc, int textIndexFrom, int textIndexTo) {
		this.doc=doc;
		this.textIndexFrom=textIndexFrom;
		this.textIndexTo=textIndexTo;
	}
	public SourceReference(Doc doc, ITreeElem t) {
		this.doc=doc;
		this.textIndexFrom=t.getTextIndexFrom();
		this.textIndexTo=t.getTextIndexTo();
	}
	public Doc getDoc() {
		return doc;
	}
	public int getTextIndexFrom() {
		return textIndexFrom;
	}
	public int getTextIndexTo() {
		return textIndexTo;
	}
	@Override
	public String toString() {
		return ""+doc.id+" "+textIndexFrom+" "+textIndexTo;
	}
	public int getLength() {
		return textIndexTo-textIndexFrom;
	}
}
