package hu.qgears.xtextgrammar;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;

import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.LanguageHelper;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.rtemplate.runtime.RQuickTemplate;

public class EnumProcessor extends AbstractProcessor {
	public EnumProcessor(ProcessXtextFile processXtextFile, RQuickTemplate out) {
		super(processXtextFile, out);
	}
	public void process(ITreeElem tree) throws Exception {
		typeName=LanguageHelper.singleChildByType(tree, "tId").getString();
		writeObject(typeName);
		write(" := ");
		boolean first=true;
		for(ITreeElem ev: LanguageHelper.getAllByType(tree, "enumValue"))
		{
			if(!first)
			{
				write("|");
			}
			first=false;
			String sr=newSubRuleName();
			writeObject(sr);
			String eId=LanguageHelper.singleChildByType(ev, "tId").getString();
			EClassifier enumType=host.findType(null, typeName);
			EEnum enu=(EEnum) enumType;
			EEnumLiteral eel=enu.getEEnumLiteral(eId);
			// System.out.println("Enumtype: ECLass: "+typeName+" "+enumType+" "+eId+" "+eel);
			ITreeElem term=LanguageHelper.childByType(ev, "tString");
			String termS=(term==null)?eId:RecognizerString.getString(term.getString());
			postProcess.add(()->{
				write("//# enumValue ");
				writeId(sr);
				write(" ");
				writeId(enu.getInstanceClassName());
				write(" ");
				writeId(eId);
				write("\n");
				write(" #");
				writeObject(sr);
				write(";\n");
				write(" ");
				writeObject(sr);
				write(" := ");
				createRuleForConstant(termS);
				write(";\n");
			});
		}
		write(";\n");
		executePostProcesses();
	}
}
