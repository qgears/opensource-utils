package hu.qgears.parser.contentassist;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermAnd;
import hu.qgears.parser.language.impl.TermOneOrMore;
import hu.qgears.parser.language.impl.TermZeroOrMore;

public class PossibleCollector {
	public Set<Term> found=new HashSet<>();
	public void collectPossible(Term te, int dotPos) {
		if(te!=null)
		{
			if(found.add(te))
			{
				switch(te.getType())
				{
				case token:
				{
					// System.out.println("Token: "+te);
					break;
				}
				case and:
				{
					TermAnd and=(TermAnd) te;
					List<Term> subs=and.getSubs();
					if(subs.size()>dotPos)
					{
						Term first=subs.get(dotPos);
						collectPossible(first, 0);
					}
					break;
				}
				case or:
				{
					// Or's are already resolved by the parser
					break;
				}
				case epsilon:
				{
					// Nothing to do
					break;
				}
				case reference:
				{
					// Nothing to do
					break;
				}
				case oneormore:
				{
					// On
					TermOneOrMore oom=(TermOneOrMore) te;
					collectPossible(oom.getSub(), 0);
					break;
				}
				case zeroormore:
				{
					// On
					TermZeroOrMore oom=(TermZeroOrMore) te;
					collectPossible(oom.getSub(), 0);
					break;
				}
				}
			}
		}
	}
}
