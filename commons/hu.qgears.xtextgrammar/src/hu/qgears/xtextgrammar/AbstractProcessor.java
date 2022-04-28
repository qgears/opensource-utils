package hu.qgears.xtextgrammar;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.parser.language.impl.TokenType;
import hu.qgears.parser.tokenizer.ITextSource;
import hu.qgears.parser.tokenizer.IToken;
import hu.qgears.parser.tokenizer.impl.TextSource;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.rtemplate.runtime.RQuickTemplate;

public class AbstractProcessor extends RQuickTemplate {
	protected ProcessXtextFile host;
	protected String typeName;
	protected List<RunnableWithException> postProcess=new ArrayList<>();
	private int idx=0;

	public AbstractProcessor(ProcessXtextFile host, RQuickTemplate parent) {
		super(parent);
		this.host=host;
	}
	
	@Override
	final protected void doGenerate() {
	}
	protected String newSubRuleName()
	{
		return typeName+"_"+idx++;
	}

	
	protected void createRuleForConstant(String unescaped) {
		// TODO always use as forced keyword: should be removed when possible
		if(host.forcedKeywords.contains(unescaped) || true)
		{
			String constRef=host.registerConstant(unescaped);
			writeObject(constRef);
			return;
		}
		boolean first=true;
		// System.out.println("String: "+unescaped);
		String remaining=unescaped;
		while(remaining.length()>0)
		{
			String idPrefix=idPrefix(remaining);
			if(idPrefix!=null)
			{
				if(!first)
				{
					write("+");
				}
				first=false;
				write("tId~=\"");
				writeObject(idPrefix);
				write("\"");
				remaining=remaining.substring(idPrefix.length()).trim();
				// System.out.println("Identifier: "+t.getText());
/*							if(t.getLength()<unescaped.length())
				{
					System.err.println("Compound: "+unescaped);
				}
				*/
			}else
			{
				String constRef=host.registerConstant(unescaped);
				writeObject(constRef);
				remaining="";
			}
		}
	}
	protected String idPrefix(String s)
	{
		ITextSource tx=new TextSource(s);
		IToken t=new RecognizerId(new TokenType("dummy")).getGeneratedToken(tx);
		if(t!=null)
		{
			String remaining=s.substring(t.getLength());
			if(remaining.length()==0 || remaining.startsWith(" "))
			{
				return t.getText().toString();
			}
		}
		return null;
	}
	protected void writeId(String id) {
		write("'");
		writeObject(id);
		write("'");
	}
	protected void executePostProcesses() throws Exception {
		while(postProcess.size()>0)
		{
			List<RunnableWithException> rs=postProcess;
			postProcess=new ArrayList<>();
			for(RunnableWithException r: rs)
			{
				r.run();
			}
		}
	}
}
