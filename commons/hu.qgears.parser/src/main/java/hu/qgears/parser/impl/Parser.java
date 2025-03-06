package hu.qgears.parser.impl;

import hu.qgears.parser.IParser;
import hu.qgears.parser.IParserReceiver;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.language.ILanguage;
import hu.qgears.parser.language.impl.Term;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.impl.TextSource;
import hu.qgears.parser.tokenizer.impl.TokenFilter;
import hu.qgears.parser.tokenizer.impl.Tokenizer;


/** Parser to parse a text. To be used once.
 */
public class Parser implements IParser {
	private ILanguage lang;
	private ParserLogger logger;
	private boolean tokenized=false;
	
	private Tokenizer tok;
	private TextSource ts;
	private TokenArray toks;
//	private List<Token> tokensUnfiltered;
	private ElemBuffer buffer;
	private TokenArray tokensUnfiltered;
	public Parser(ILanguage lang, String text,
			ParserLogger logger) {
		this.lang = lang;
		this.ts = new TextSource(text);
		this.tok=new Tokenizer(lang.getTokenizerDef());
		this.logger=logger;
	}

	@Override
	public TokenArray tokenize(IParserReceiver receiver)
			throws ParseException {
		if(!tokenized)
		{
			logger.logStart();
			tokensUnfiltered=new TokenArray(ts, lang);
			tok.tokenize(tokensUnfiltered, ts, receiver);
			if(receiver!=null)
			{
				receiver.tokensUnfiltered(tokensUnfiltered);
				logger.logTokenizedUnfiltered();
			}
			toks = new TokenFilter(lang.getTokenFilterDef()).filter(tokensUnfiltered);
			logger.logTokenized();
			tokenized=true;
		}
		return toks;
	}
	@Override
	public TreeElem parse(IParserReceiver receiver) throws ParseException {
		if(receiver==null)
		{
			receiver=new DefaultReceiver();
		}
		// Be sure to have the text tokenized.
		TokenArray tokens=tokenize(receiver);
		receiver.tokens(tokens);
		TextSource src=ts;
		Term[] terms = lang.getTerms();
		// Create a buffer for registering early parsing elements
		if(buffer==null)
		{
			buffer=new ElemBuffer();
		}
		buffer.reInit(terms, tokens, lang);
		// Create element generation rules
		// Generate element of the sentence symbol.
		GenerationRules.generateNonTerm(buffer, lang.getRootTerm(), tokens, 0, -1);
		// Generate all elements for all tokens
		for (int tokenIndex = 0; tokenIndex < tokens.size(); ++tokenIndex) {
			// Token t = tokens.get(tokenIndex);
			for (int i=buffer.getCurrentGroupStart();i<buffer.getCurrentGroupEnd();++i)
			{
				GenerationRules.generateOnSameGroup(i,tokens, tokenIndex, buffer);
			}
			if (tokenIndex >= tokens.size() - 1)
			{
				break;
			}
			int changeCount = 0;
			{
				int from=buffer.getCurrentGroupStart();
				int to=buffer.getCurrentGroupEnd();
				buffer.newGroup();
				for(int i=from;i<to;++i)
				{
					changeCount += GenerationRules.generateOnNextGroup(buffer, i, tokens, tokenIndex);
				}
			}
			if (changeCount == 0) {
				logger.logStateWhenParseStuck(buffer, src);
				receiver.stucked(buffer, tokens, tokenIndex);
				return null;	// No tree because we could not build it
			}
		}
		// Table is filled.
		logger.logTableFilled(buffer, buffer.getSize(), buffer.getCurrentGroup(), tokens
				.size());
		receiver.tableFilled(buffer, tokens.size());
		// Now find the real generation tree of the sentence.
		if (buffer.contains(buffer.getCurrentGroupStart(), buffer.getCurrentGroupEnd(), 1, lang.getRootTerm().getId(), 0, 0)) {
			// parse successful!
			TreeElem root = new TreeElem(buffer, 1, lang.getRootTerm().getId(), 0, 0, buffer.getCurrentGroup());
			// System.out.println(root);
			new BuildTree(logger).buildTreeRoot(root);
			logger.logTreeBuild();
			receiver.treeUnfiltered(root);
			// System.out.println(new TreeRenderer().render2(root, ""));
			new TreeFilter().filter(root, lang.getTermFilterDef());
			logger.logTreeFiltered();
			receiver.treeFiltered(root);
			return root;
			// System.out.println(new TreeRenderer().render2(root, ""));
			// Now we have the data we have to build the
			// syntax tree!
		}else
		{
			receiver.parseProblemUnknown(buffer);
			return null;
		}
	}
	@Override
	public TokenArray getTokensUnfiltered() {
		return tokensUnfiltered;
	}

	@Override
	public void setBuffer(ElemBuffer buffer) {
		this.buffer=buffer;
	}
}
