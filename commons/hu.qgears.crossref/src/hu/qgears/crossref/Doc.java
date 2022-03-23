package hu.qgears.crossref;

import java.util.HashSet;
import java.util.Set;

/**
 * A document is a compile unit, a single file that is rebuilt when a file has changed within the scope of the
 * incremental builder.
 */
public class Doc extends CrossRefObject {
	public final String id;
	protected Set<Ref> refs=new HashSet<>();
	protected Set<Obj> objs=new HashSet<>();
	public Doc(CrossRefManager crossRefManager, String identifier) {
		super(crossRefManager);
		id=identifier;
	}
	@Override
	public Doc getDoc() {
		return this;
	}
}
