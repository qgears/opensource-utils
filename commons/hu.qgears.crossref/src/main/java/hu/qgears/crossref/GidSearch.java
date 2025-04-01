package hu.qgears.crossref;

/**
 * A search by a reference. A single reference may have multiple searches.
 */
public class GidSearch {
	public final String gid;
	public final Ref ref;
	public final int priority;
	public GidSearch(String gid, Ref ref, int priority) {
		super();
		this.gid = gid;
		this.ref = ref;
		this.priority = priority;
	}
}
