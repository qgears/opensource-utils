package hu.qgears.crossref;

/**
 * Additional filter to check whether a possible delegate for a reference target is a valid target or not.
 * This is useful in cases when the filtering function is not possible to be presented to the crossRef system
 * in a declarative way.
 */
@FunctionalInterface
public interface IFilterFunction {
	/**
	 * Check whether a possible delegate for a reference target is a valid target or not.
	 * @param ref The reference
	 * @param target The Object which might be a valid target.
	 * @return false means this delegate will not be reported as a found target.
	 */
	boolean isPossibleTarget(Ref ref, Obj target);
}
