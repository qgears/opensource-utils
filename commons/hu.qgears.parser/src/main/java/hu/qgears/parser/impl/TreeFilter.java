package hu.qgears.parser.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.qgears.parser.language.ITermFilterDef;
import hu.qgears.parser.language.impl.Term;



public class TreeFilter {
	boolean remains[];

	public void filter(TreeElem root, ITermFilterDef def) {
		remains = new boolean[root.getBuffer().getTerms().length];
		for (Term t : root.getBuffer().getTerms()) {
			remains[t.getId()] = def.getRemainingTerms().contains(t.getName());
		}
		remains[root.getBuffer().getLang().getRootTerm().getId()] = true;
		filtered(root);
	}

	private List<TreeElem> filteredAll(List<TreeElem> roots) {
		List<TreeElem> ret = new ArrayList<TreeElem>();
		for (TreeElem e : roots) {
			ret.addAll(filtered(e));
		}
		return ret;
	}

	private List<TreeElem> filtered(TreeElem root) {
		if (remains[root.getType().getId()]) {
			List<TreeElem> children = filteredAll(root.getSubs());
			root.setSubs(children);
			return Collections.singletonList(root);
		} else {
			return filteredAll(root.getSubs());
		}
	}
}
