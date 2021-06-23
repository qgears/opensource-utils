package hu.qgears.parser.test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hu.qgears.parser.language.impl.LanguageParserXML;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermCompound;
import hu.qgears.parser.language.impl.TermMore;
import hu.qgears.parser.language.impl.TermRef;

/**
 * TODO This feature is not used. Should it be removed?
 * @author rizsi
 *
 */
public class LanguageWriter {
	public String write(List<? extends Term> l) {
		StringBuilder out = new StringBuilder();
		for (Term t : l) {
			if (t.getName().indexOf(LanguageParserXML.uniqueDiv) < 0) {
				String w = write(t, new TreeSet<String>());
				out.append(w);
				out.append("\n");
			}
		}
		return out.toString();
	}

	public String write(Term t) {
		return write(t, new TreeSet<String>());
	}

	String write(Term t, Set<String> alreadyWritten) {
		StringBuilder out = new StringBuilder();
		if (t == null) {
			throw new RuntimeException();
		}
		out.append(t.getName() + "\t:" + t.getType());
		if (!alreadyWritten.contains(t.getName())) {
			alreadyWritten.add(t.getName());
			switch (t.getType()) {
			case and:
			case or: {
				TermCompound c = (TermCompound) t;
				out.append("(");
				for (Term s : c.getSubs()) {
					out.append(write(s, alreadyWritten));
					out.append(",");
				}
				out.append(")");
				break;
			}
			case reference: {
				TermRef m = (TermRef) t;
				out.append("(");
				if (m.getSub() == null) {
					throw new RuntimeException("error in language: no child: "
							+ t.getName() + "\t:" + t.getType());
				}
				out.append(write(m.getSub(), alreadyWritten));
				out.append(")");
				break;
			}
			case oneormore:
			case zeroormore: {
				TermMore m = (TermMore) t;
				out.append("(");
				out.append(write(m.getSub(), alreadyWritten));
				out.append(")");
				break;
			}
			case epsilon:
			case token:
				break;
			}
		}
		return out.toString();
	}
}
