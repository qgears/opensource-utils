package hu.qgears.parser.test.contentassist;

import java.util.HashSet;
import java.util.List;

import hu.qgears.parser.IParser;
import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.LanguageHelper;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.contentassist.ICompletitionProposalContext;
import hu.qgears.parser.contentassist.PossibleGoon;
import hu.qgears.parser.impl.BuildTree;
import hu.qgears.parser.impl.ElemBuffer;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.language.impl.TermToken;
import hu.qgears.parser.math.MathExpressionLanguage;
import hu.qgears.parser.tokenizer.Token;

public class DummyProposalContext implements ICompletitionProposalContext {

	ILanguage lang=MathExpressionLanguage.getInstance().getLang();

	@Override
	public void parse(String text, IParserReceiver iParserReceiver) throws Exception {
		IParser p = LanguageHelper.createParser(lang, text, new ParserLogger());
		p.setBuffer(new ElemBuffer());
		p.parse(iParserReceiver);
	}

	@Override
	public boolean isFiltered(Token t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean collectAllowedPrefixes(PossibleGoon collect, Term t, ElemBuffer buffer, String prefix,
			List<String> context, List<Term> parents) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logError(Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void collectPossibilities(PossibleGoon collect, TermToken termType, String prefix) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void notifyParseState(ElemBuffer buffer, int i) {
		BuildTree bt=new BuildTree(new ParserLogger());
		bt.incompleteTree(buffer, i, new HashSet<>(), "");
		ICompletitionProposalContext.super.notifyParseState(buffer, i);
	}
}
