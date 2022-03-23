package hu.qgears.crossref;

import java.util.HashSet;
import java.util.Set;

public class Obj extends CrossRefObject {
	Doc doc;
	String localId;
	String type;
	String typeAndLocalId;
	String fqId;
	protected Set<Ref> referencesTargetingThis=new HashSet<>(); 
	public Obj(Doc doc, String fqId, String type) {
		super(doc.getHost());
		this.doc=doc;
		localId=getHost().getLocalId(fqId);
		this.type=type;
		this.typeAndLocalId=getHost().getTypeAndLocalId(type, getLocalId());
		this.fqId=fqId;
	}
	public String getLocalId() {
		return localId;
	}
	public String getTypeAndLocalId() {
		return typeAndLocalId;
	}
	public String getFqId() {
		return fqId;
	}
	public String getType() {
		return type;
	}
	@Override
	public Doc getDoc() {
		return doc;
	}
}
