package hu.qgears.tools;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilString;

public class Tools {
	public static void main(String[] args) {
		List<String> largs=new ArrayList<>();
		for(String a: args)
		{
			largs.add(a);
		}
		int ret=new Tools().exec(largs);
		System.exit(ret);
	}
	private static List<ITool> tools=new ArrayList<>();
	private List<ITool> createTools()
	{
		List<ITool> ret=new ArrayList<>();
		ret.add(new SrvAdmin());
		ret.add(new GitToZip());
		ret.add(new GitBackupUpdate());
		ret.add(new SvnDiff());
		ret.addAll(tools);
		return ret;
	}
	private int exec(List<String> args){
		try {
			if(args.size()>0)
			{
				if(args.get(0).equals("help"))
				{
					if(args.size()>1)
					{
						for(ITool t: createTools())
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
				for(ITool t: createTools())
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
				for(ITool t: createTools())
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
		tools.add(tool);
	}
}
