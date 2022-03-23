package hu.qgears.crossref;

import java.util.List;

/**
 * Listen to cross reference resolve events.
 */
public interface IRefListener extends ICrossRefObjectListener {
	/**
	 * Signal that the reference was resolved to these objects.
	 * The method is also called in case the resolve result was that there is no target found.
	 * @param target may be null in case of unresolved reference. The internal list is received here that must be used read only!
	 */
	default void resolvedTo(List<Obj> target)
	{}
}
