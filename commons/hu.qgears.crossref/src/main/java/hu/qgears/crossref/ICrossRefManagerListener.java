package hu.qgears.crossref;

import java.util.Set;

/**
 * Listen for cross reference resolver cycles and transactions.
 */
public interface ICrossRefManagerListener {
	/**
	 * Transaction is finished: all references are resolved that are possible in the current state.
	 * Change listeners are notified before this.
	 */
	default void transactionFinished(){}
	/**
	 * A single run of resolving references was finished within a transaction.
	 * Objects that generate other objects after being resolved are possible to be executed within this listener.
	 */
	default void resolveCycleFinished(){}
	/**
	 * References in this document were changed in this transaction.
	 * @param d
	 * @param changes 
	 */
	default void documentChanged(Doc d, Set<CrossRefObject> changes){}
}
