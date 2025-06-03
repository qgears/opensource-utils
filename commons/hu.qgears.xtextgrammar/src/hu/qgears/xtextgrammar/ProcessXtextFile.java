package hu.qgears.xtextgrammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.impl.DefaultReceiver;
import hu.qgears.parser.tokenizer.Token;
import hu.qgears.parser.tokenizer.TokenArray;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.parser.util.TreeVisitor;
import hu.qgears.rtemplate.runtime.RQuickTemplate;

public class ProcessXtextFile {
	private Map<String, String> constants=new TreeMap<>();
	private Map<String, String> outsideDefinedConstants=new TreeMap<>();
	private Map<String, String> importsWithName=new TreeMap<>();
	private Set<String> importsNoName=new TreeSet<>();
	public Set<String> omitRule=new HashSet<>();
	public Set<String> keywordTerminals=new TreeSet<>();
	private int terminalCounter;
	public String qGrammar;
	public String firstRuleName;
	public static final String localCrossReference="crossReferenceLocal";
	public static final String fqidCrossReference="crossReferenceFqid";

	public ProcessXtextFile(ClassLoader classLoader) {
		// TODO Auto-generated constructor stub
	}

	public void process(String exampleXtext) throws Exception {
		ITreeElem t=new XtextGrammarParser().parse(exampleXtext, new DefaultReceiver() {
			@Override
			public void tokensUnfiltered(TokenArray tokensUnfiltered) {
				for(int i = 0; i < tokensUnfiltered.size(); i++)
				{
					Token t = tokensUnfiltered.getToken(i);
//					System.out.println("TokenType: "+t.getTokenType().getName());
//					System.out.println("Token: "+t.getText());
				}
				super.tokensUnfiltered(tokensUnfiltered);
			}
			
		});
		process(t);
	}

	public void process(ITreeElem t) throws Exception {
		RQuickTemplate out=new RQuickTemplate() {@Override
		protected void doGenerate() {
		}};
		// LanguageHelper.print(t);
		new TreeVisitor() {
			
			@Override
			protected AutoCloseable visitNode(ITreeElem tree, int depth) throws Exception {
				switch(tree.getTypeName())
				{
				case "doc":
					return ()->{};
				case "stImport":
					String imp=RecognizerString.getString(tree.getSubs().get(0).getString());
					if(tree.getSubs().size()>1)
					{
						String as=RecognizerId.unescape(tree.getSubs().get(1).getString(), "^");
						importsWithName.put(as, imp);
					}else
					{
						importsNoName.add(imp);
					}
					return null;
				case "stGrammar":
				case "stTerminal":
					// ignore this subtree
					return null;
				case "stRule":
					RuleProcessor rp=new RuleProcessor(ProcessXtextFile.this, out);
					rp.process(tree);
					if(firstRuleName==null)
					{
						firstRuleName=rp.typeName;
					}
					// System.out.println(""+rp.generate());
					return null;
				case "stEnum":
					new EnumProcessor(ProcessXtextFile.this, out).process(tree);
					parseEnum(tree);
					return null;
				default:
					System.err.println("unhandled type: "+tree.getTypeName());
					return null;
				}
			}
		}.visit(t);
		RQuickTemplate ready=new RQuickTemplate() {
			@Override
			protected void doGenerate() {
				List<String> keys=new ArrayList<>(constants.keySet());
				Collections.reverse(keys); // In this ordering if one keyword is the continuitation of an other then that will be first in order and first matched
				for(String s: keys)
				{
					String type;
					if(isIdentifier(s))
					{
						type="wholeWord";
						keywordTerminals.add(constants.get(s));
					}else
					{
						type="const";
					}
					writeObject(constants.get(s));
					write("=\"");
					writeObject(type);
					write("\" \"");
					write(s);
					write("\";\n");
				}
				writeObject(out.generate());
			}
		};
		qGrammar=ready.generate();
	}
	protected boolean isIdentifier(String s) {
		if(s.length()>0)
		{
			if(Character.isJavaIdentifierStart(s.charAt(0)))
			{
				for(int i=1;i<s.length();++i)
				{
					if(!Character.isJavaIdentifierPart(s.charAt(i)))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public String registerConstant(String c)
	{
		if(outsideDefinedConstants.containsKey(c))
		{
			return outsideDefinedConstants.get(c);
		}
		if(constants.containsKey(c)) {
			return constants.get(c);
		}
		String idDelegate="TERMINAL_"+terminalCounter++;
		if(isIdentifier(c)) {
			idDelegate+=c;
		}
		constants.put(c, idDelegate);
		return idDelegate;
	}

	protected void parseEnum(ITreeElem tree) {
		// TODO Auto-generated method stub
		
	}
	protected Set<String> forcedKeywords=new HashSet<>();

	public void forceKeyword(String string) {
		forcedKeywords.add(string);
	}

	public void setTerminalId(String value, String terminalName) {
		outsideDefinedConstants.put(value, terminalName);
	}

	public void omitRule(String string) {
		omitRule.add(string);
	}

	public EClassifier findType(String scope, String id) {
		if(scope!=null)
		{
			String imp=importsWithName.get(scope);
			// String fqid=imp+"."+id;
			// System.out.println("load: "+fqid);
			EPackage pack=(EPackage)EPackage.Registry.INSTANCE.get(imp);
			EClassifier cla=pack.getEClassifier(id);
			return cla;
		}
		for(String s: importsNoName)
		{
			EPackage pack=(EPackage)EPackage.Registry.INSTANCE.get(s);
			if(pack==null)
			{
				throw new RuntimeException("import Missing package: "+s);
			}
			EClassifier cla=pack.getEClassifier(id);
			if(cla!=null)
			{
				return cla;
			}
		}
		return null;
	}

	public void addTerminalIds(String genericPartPre) {
		RuntimeMappings rm=new RuntimeMappings().process(genericPartPre);
		for(Map.Entry<String, String> ent: rm.consts.entrySet())
		{
			setTerminalId(ent.getKey(), ent.getValue());
		}
	}
}
