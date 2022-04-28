package hu.qgears.xtextgrammar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClassifier;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.LanguageHelper;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.rtemplate.runtime.RQuickTemplate;

public class RuleProcessor extends AbstractProcessor {
	private EClassifier cla;
	public RuleProcessor(ProcessXtextFile host, RQuickTemplate out) {
		super(host, out);
	}
	

	public void process(ITreeElem tree) throws Exception {
		typeName=tree.getSubs().get(0).getString();
		if(host.omitRule.contains(typeName))
		{
			return;
		}
		ITreeElem retType=LanguageHelper.childByType(tree, "ruleReturnTypeSpec");
		if(retType!=null)
		{
//			LanguageHelper.print(retType);
			ITreeElem scopedId=LanguageHelper.singleChildByType(retType, "scopedId");
			ITreeElem scopedOrNothing=LanguageHelper.childByType(scopedId, "scopeOrNothing");
			String scope=null;
			if(scopedOrNothing.getSubs().size()>0)
			{
				scope=scopedOrNothing.getSubs().get(0).getString();
			}
			String id=RecognizerId.unescape(LanguageHelper.singleChildByType(scopedId, "tId").getString(), "^");
			cla=host.findType(scope, id);
		}else
		{
			cla=host.findType(null, typeName);
		}
//		LanguageHelper.print(tree);
		ITreeElem re=LanguageHelper.singleChildByType(tree, "ruleExpression");
		ITreeElem rec=LanguageHelper.singleChild(re);
		write("#");
		writeObject(typeName);
		write(";\n");
		writeObject(typeName);
		write(" := ");
		parseExpression(rec, 0);
		write(";\n");
		executePostProcesses();
		if(cla!=null)
		{
			write("//# type ");
			writeId(typeName);
			write(" ");
			writeId(cla.getInstanceClassName());
			write("\n");
		}
//		while(subrules.size()>0)
//		{
//			Map<String, ITreeElem> toprocess=subrules;
//			subrules=new TreeMap<>();
//			for(String key: toprocess.keySet())
//			{
//				write(" ");
//				writeObject(key);
//				write(" := ");
//				parseExpression(toprocess.get(key));
//				write(";\n");
//			}
//		}
	}
	/**
	 * 
	 * @param tree
	 * @param inOption shows whether the context is optional: 0:not 1:yes 2:optional_and_repeat
	 * @throws Exception
	 */
	private void parseExpression(ITreeElem tree, int inOption) throws Exception
	{
			switch(tree.getTypeName())
			{
				case "expAddEqu":
				{
					ITreeElem exp=LanguageHelper.childByIndexAndAssertSize(tree,1, 2);
					String id=newSubRuleName();
//					subrules.put(id, exp);
					writeObject(id);
					//ExpAddEqu ret=new ExpAddEqu();
					String feature=LanguageHelper.singleChildByType(tree, "tId").getString();
					postProcess.add(()->{
						write("//# addToFeature ");
						writeId(id);
						write(" ");
						writeId(getCurrentType());
						write(" ");
						writeId(feature);
						write("\n");
						write(" #");
						writeObject(id);
						write(";\n");
						write(" ");
						writeObject(id);
						write(" := ");
						parseExpression(exp, 0);
						write(";\n");
					});
					// TODO 
					return;
				}
				case "expQEqu":
				{
					ITreeElem exp=LanguageHelper.childByIndexAndAssertSize(tree,1, 2);
					String id=newSubRuleName();
//					subrules.put(id, exp);
					write("");
					writeObject(id);
					//ExpAddEqu ret=new ExpAddEqu();
					String feature=LanguageHelper.singleChildByType(tree, "tId").getString();
					String type=getCurrentType();
					postProcess.add(()->{
						write("//# setBooleanTrue ");
						writeId(id);
						write(" ");
						writeId(type);
						write(" ");
						writeId(feature);
						write("\n");
						write(" #");
						writeObject(id);
						write(";\n");
						write(" ");
						writeObject(id);
						write(" := ");
						parseExpression(exp, 0);
						write(";\n");
					});
					// TODO 
					return;
				}
				case "expLet":
				{
					ITreeElem exp=LanguageHelper.childByIndexAndAssertSize(tree,1, 2);
					String id=newSubRuleName();
					// subrules.put(id, exp);
					writeObject(id);
					//ExpAddEqu ret=new ExpAddEqu();
					String feature=LanguageHelper.singleChildByType(tree, "tId").getString();
					String type=getCurrentType();
					postProcess.add(()->{
						write("//# setFeature ");
						writeId(id);
						write(" ");
						writeId(type);
						write(" ");
						writeId(feature);
						write(" to: \n");
						write(" #");
						writeObject(id);
						write(";\n");
						write(" ");
						writeObject(id);
						write(" := ");
						parseExpression(exp, 0);
						write(";\n");
					});
					return;
				}
				case "reference":
				{
					List<String> refTypes=new ArrayList<>();
					for(ITreeElem te: tree.getSubs())
					{
						refTypes.add(te.getString());
					}
					if(refTypes.size()==1)
					{
						write(host.localCrossReference);
					}else if(refTypes.size()==2)
					{
						write(host.fqidCrossReference);
					}else
					{
						throw new RuntimeException();
					}
					break;
				}
				case "expConcatenate":
				{
					write("");
					parseExpression(LanguageHelper.childByIndexAndAssertSize(tree, 0, 2), 0);
					write("+");
					parseExpression(LanguageHelper.childByIndexAndAssertSize(tree, 1, 2), 0);
					write("");
					return;
				}
				case "any":
					if(inOption==1)
					{
						write("*1");
					}else if(inOption==2)
					{
						// No repeat allowed here
					}else
					{
						write("*");
					}
					parseExpression(LanguageHelper.singleChild(tree), 1);
					write("");
					return;
				case "zeroOrOne":
					if(inOption>0)
					{
						// Already in optional grammar node: zero or more would make parse ambiguous
						parseExpression(LanguageHelper.singleChild(tree), 2);
					}else
					{
						write("(!|(");
						parseExpression(LanguageHelper.singleChild(tree), 1);
						write("))");
					}
					return;
				case "expOr":
					write("(");
					parseExpression(LanguageHelper.childByIndexAndAssertSize(tree, 0, 2), 1);
					write(")|(");
					parseExpression(LanguageHelper.childByIndexAndAssertSize(tree, 1, 2), 1);
					write(")");
					return;
				case "variable":
					writeObject(tree.getString());
					// System.out.println("Variable: "+tree.getString());
					return;
				case "createEObject":
				{
					String id=newSubRuleName();
					writeObject(id);
					String createType=RecognizerId.unescape(tree.getSubs().get(0).getString(), "^");
					EClassifier cla=host.findType(null, createType);
					this.cla=cla;
					String type=getCurrentType();
					postProcess.add(()->{
						write("//# createType ");
						writeId(id);
						write(" ");
						writeId(type);
						write("\n");
						write(" #");
						writeObject(id);
						write(";\n");
						write(" ");
						writeObject(id);
						write(" := !;\n");
					});
					return;
				}
				case "setHostParameter":
				{
					String id=newSubRuleName();
					writeObject(id);
					String createType=RecognizerId.unescape(tree.getSubs().get(0).getString(), "^");
					String feature=RecognizerId.unescape(tree.getSubs().get(1).getString(), "^");
					EClassifier cla=host.findType(null, createType);
					this.cla=cla;
					postProcess.add(()->{
						write("//# createTypeSetFeatureCurrent ");
						writeId(id);
						write(" ");
						writeId(cla.getInstanceClassName());
						write(" ");
						writeId(feature);
						write("\n");
						write(" #");
						writeObject(id);
						write(";\n");
						write(" ");
						writeObject(id);
						write(" := !;\n");
					});
					return;
				}
				case "constString":
				{
					String unescaped=RecognizerString.getString(tree.getString());
					createRuleForConstant(unescaped);
					return;
				}
				case "unorderedOption":
				{
					boolean first=true;
				// case "optional":
					if(isAllOptional(tree.getSubs()))
					{
						write("*(");
					}else
					{
						write("*1(");
					}
					for(ITreeElem s: tree.getSubs())
					{
						if(!first)
						{
							write("|");
						}
						parseExpression(s, 2);
						first=false;
					}
					write(")");
					return;
				}
				case "oneOrMore":
					write("*1(");
					parseExpression(LanguageHelper.singleChild(tree), 2);
					write(")");
					return;
				case "bracketed":
					EClassifier prevCla=cla;
					write("(");
					parseExpression(LanguageHelper.singleChild(tree), inOption);
					write(")");
					if(prevCla!=null)
					{
						// Restore type in case {InstClass.left=current} mechanism was used.
						cla=prevCla;
					}
					return;
				default:
					throw new RuntimeException("Unhandled: "+tree.getTypeName());
				}
//		System.out.println("Rule: ");
		//LanguageHelper.print(tree);
	}

	private String getCurrentType() {
		return cla.getInstanceClassName();
	}


	private boolean isAllOptional(List<? extends ITreeElem> subs) {
		boolean ret=true;
		for(ITreeElem te: subs)
		{
			ret&=isOptional(te);
		}
		return ret;
	}

	private boolean isOptional(ITreeElem te) {
		switch(te.getTypeName())
		{
		case "expAddEqu":
		{
			return false;
		}
		case "expQEqu":
		{
			return false;
		}
		case "expLet":
		{
			return false;
		}
		case "reference":
		{
			return false;
		}
		case "expConcatenate":
		{
			return isOptional(LanguageHelper.childByIndexAndAssertSize(te, 0, 2))&&
					isOptional(LanguageHelper.childByIndexAndAssertSize(te, 1, 2));
		}
		case "any":
			return true;
		case "zeroOrOne":
			return true;
		case "expOr":
			return isOptional(LanguageHelper.childByIndexAndAssertSize(te, 0, 2))||
					isOptional(LanguageHelper.childByIndexAndAssertSize(te, 1, 2));
		case "createEObject":
			return false;
		case "setHostParameter":
			return false;
		case "constString":
		{
			return false;
		}
		case "unorderedOption":
		{
			return isAllOptional(te.getSubs());
		}
		case "oneOrMore":
			return false;
		case "bracketed":
			return isOptional(LanguageHelper.singleChild(te));
		default:
			throw new RuntimeException("Unhandled: "+te.getTypeName());
		}
	}
}
