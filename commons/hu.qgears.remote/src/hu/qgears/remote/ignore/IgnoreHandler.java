package hu.qgears.remote.ignore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.NoExceptionAutoClosable;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;

/**
 * ignore file handler - a simplified implementation that is just good enough for our goals. 
 */
public class IgnoreHandler {
	List<IgnoreRule> rules=new ArrayList<IgnoreRule>();
	public NoExceptionAutoClosable processIgnore(File ignoreFile, int depth) throws IOException {
		int n=rules.size();
		if(ignoreFile.isFile())
		{
			String s=UtilFile.loadAsString(ignoreFile);
			for (String line: UtilString.split(s, "\r\n"))
			{
				if(line.startsWith("#"))
				{
					continue;
				}
				System.out.println("Add ignore rule: "+depth+" "+line);
				rules.add(new IgnoreRule(depth, line));
			}
		}
		return new NoExceptionAutoClosable() {
			@Override
			public void close() {
				while(rules.size()>n)
				{
					rules.remove(rules.size()-1);
				}
			}
		};
	}

	public boolean isIgnored(int depth, String localPath, String name, boolean directory) {
		EResult ret=EResult.impliciteOk;
		for(IgnoreRule r: rules)
		{
			ret=r.isIgnored(depth, localPath, name, directory, ret);
		}
		return ret.isIgnored();
	}

}
