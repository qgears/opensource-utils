package hu.qgears.parser.language;

import java.util.Set;

/**
 * non-Term filter definition. The symbols are filtered after parsing.
 * 
 * @author rizsi
 *
 */
public interface ITermFilterDef {
	/**
	 * Terms that are not in this list are filtered out from the result tree.
	 * @return
	 */
	Set<String> getRemainingTerms();
}
