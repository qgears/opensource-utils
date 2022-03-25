package hu.qgears.crossref;
/**
 * Listen for cross reference resolver cycles and transactions.
 */
public interface ICrossRefManagerListener {
	/**
	 * Transaction is finished: all references are resolved that are possible in the current state.
	 */
	void transactionFinished();
	/**
	 * A single run of resolving references was finished within a transaction.
	 * Objects that generate other objects after being resolved are possible to be executed within this listener.
	 */
	void resolveCycleFinished();
}
