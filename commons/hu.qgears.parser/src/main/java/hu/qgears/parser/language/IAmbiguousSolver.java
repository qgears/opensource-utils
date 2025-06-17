package hu.qgears.parser.language;

import java.util.List;

import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.impl.TreeElem;
import hu.qgears.parser.language.impl.Term;

public interface IAmbiguousSolver {
	/**
	 * Called when parse is ambiguous before throwing exception.
	 * @param types
	 * @param from
	 * @param to
	 * @param buf
	 * @param sub
	 * @return null means no solution, returned 1 element list is the solution.
	 */
	default public List<TreeElem> solveAmbiguousParse(List<Term> types, int from, int to, ElemBuffer buf, List<List<TreeElem>> sub) {
		return null;
	}
}
