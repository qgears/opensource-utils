package hu.qgears.tools;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilString;
import hu.qgears.tools.rtemplate.RTemplateStandalone;

public class Tools {
	private static Tools instance=new Tools().populateDefault();
	public static void main(String[] args) {
		int ret=instance.mainEntryPoint(args);
		System.exit(ret);
	}
	public int mainEntryPoint(String[] args) {
		List<String> largs=new ArrayList<>();
		for(String a: args)
		{
			largs.add(a);
		}
		int ret=exec(largs);
		return ret;
	}
	private List<ITool> tools=new ArrayList<>();
	private Tools populateDefault()
	{
		tools.add(new SrvAdmin());
		tools.add(new GitToZip());
		tools.add(new GitBackupUpdate());
		tools.add(new SvnDiff());
		tools.add(new RTemplateStandalone());
		tools.add(new LogPortForward());
		return this;
	}
	private int exec(List<String> args){
		try {
			if(args.size()>0)
			{
				if(args.get(0).equals("help"))
				{
					if(args.size()>1)
					{
						for(ITool t: tools)
						{
							if(args.get(1).equals(t.getId()))
							{
								return t.help(args.subList(2, args.size()));
							}
						}
					}else
					{
						System.err.println("Help command must be specified");
						return 1;
					}
				}
				for(ITool t: tools)
				{
					if(args.get(0).equals(t.getId()))
					{
						return t.exec(args.subList(1, args.size()));
					}
				}
				System.err.println("Tool not exist: "+args.get(0));
				return 1;
			}else
			{
				System.out.println("Q-Gears command line tools");
				System.out.println("Tool not specified.\n");
				System.out.println("Help: $ java -jar tools.jar help {toolId}\n");
				System.out.println("Available tools:");
				System.out.println("");
				for(ITool t: tools)
				{
					System.out.println(""+t.getId()+": "+head(t.getDescription()));
				}
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	/**
	 * First line and first 30 characters.
	 * @param description
	 * @return
	 */
	private String head(String description) {
		try {
			String s=UtilString.split(description, "\r\n").get(0);
			if(s.length()>60)
			{
				return s.substring(0, 70);
			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public static void registerTool(ITool tool) {
		instance.register(tool);
	}
	public void register(ITool tool) {
		tools.add(tool);
	}
	public static Tools getInstance() {
		return instance;
	}
}
