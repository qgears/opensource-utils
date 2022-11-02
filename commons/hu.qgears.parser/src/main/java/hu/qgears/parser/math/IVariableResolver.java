package hu.qgears.parser.math;

/**
 * Interface for providing logic that returns the numerical value of a variable within an math expression.
 */
@FunctionalInterface
public interface IVariableResolver {

	/**
	 * Return the value of the variable named "variableName". Might return null if
	 * the given variable is undefined or unknown.
	 * 
	 * @param vaiableName
	 * @return
	 */
	Double resolveVar(String vaiableName);
}
