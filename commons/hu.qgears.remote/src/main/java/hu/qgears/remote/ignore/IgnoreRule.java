package hu.qgears.remote.ignore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IgnoreRule {
	public IgnoreRule(int depth2, String line) {
		pattern=line;
		if(pattern.startsWith("/"))
		{
			pattern=pattern.substring(1);
			startsWithFolder=true;
		}
		if(pattern.endsWith("/"))
		{
			pattern=pattern.substring(0, pattern.length()-1);
			endsWithFolder=true;
		}
		pattern="^"+pattern+"$";
		patternO = Pattern.compile(pattern);
	}
	Pattern patternO;
	public String pattern;
	public int depth;
	public boolean startsWithFolder;
	public boolean endsWithFolder;
	public EResult isIgnored(int depth, String localPath, String name, boolean directory, EResult ret) {
		if(startsWithFolder)
		{
			if(depth==this.depth+1)
			{
				return match(name, ret);
			}
		}
		// System.out.println("Isignored: "+this.depth+" "+pattern+" "+depth+" "+name+" "+ret);
		return ret;
	}
	private EResult match(String name, EResult ret) {
		Matcher m=patternO.matcher(name);
		if(m.find())
		{
			// System.out.println("Match: "+pattern+" "+name);
			return EResult.expliciteNot;
		}else{
			return ret;
		}
	}
}
