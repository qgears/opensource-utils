package hu.qgears.tools;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilString;
import hu.qgears.tools.GitBackupUpdate.Args;
import hu.qgears.tools.UtilProcess2.ProcessFuture;
import hu.qgears.tools.UtilProcess2.ProcessResult;

public class BackupSingleGitRepo {
	private File f;
	private TemplateDelegate t=new TemplateDelegate();
	private Args a;
	private Calendar c;
	private String dateformatTemplate="yyyy.MM.dd.HH-mm-ss";
	private SimpleDateFormat df=new SimpleDateFormat(dateformatTemplate);
	private String backupRefPrefix;
	private boolean hasChange=false;

	/**
	 * Ordered by name - for a single reference name that means order by date!
	 */
	private Map<String, String> refNameToHash=new TreeMap<>();

	public BackupSingleGitRepo(File f, Args a, Calendar c) {
		super();
		this.f = f;
		this.a = a;
		this.c = c;
		backupRefPrefix = "refs/"+a.backupRef+"/";
	}
	private String currentUrl;

	private boolean error=false;
	private TreeSet<String> oldRefsToDelete=new TreeSet<>();
	// private TreeSet<String> newRefs=new TreeSet<>();
	// private TreeSet<String> untilRefs=new TreeSet<>();
	private TreeSet<Long> allKnowDates=new TreeSet<>();
	private Set<Pair<String,String>> existsByBackup=new HashSet<>();
	private Map<Pair<String,String>, TreeSet<Long>> byUrlHashDates=new HashMap<>();
	private Map<Pair<String,String>, TreeSet<Long>> byUrlHashFrom=new HashMap<>();
	private Map<Pair<String,String>, TreeSet<Long>> byUrlHashUntil=new HashMap<>();
	public void backup() throws Exception
	{
		error=false;
		write("\n== : ");
		writeObject(f.getName());
		write("\n\n....\n");
		try
		{
			{
				Process p=Runtime.getRuntime().exec("git remote get-url origin", null, f);
				ProcessFuture pr=UtilProcess2.execute(p);
				currentUrl=UtilString.split(pr.get(a.timeoutMillis, GitBackupUpdate.timeoutUnit).getStdoutString(), "\r\n").get(0);
				write("Remote URL: ");
				writeObject(currentUrl);
				write("\n");
			}
			{
				StringBuilder refspec=new StringBuilder("git fetch -p origin");
				for(String r: a.refs)
				{
					refspec.append(" ");
					refspec.append("+refs/"+r+"/*:refs/"+r+"/*");
				}
				addLog(refspec.toString());
				Process p=Runtime.getRuntime().exec(refspec.toString(), null, f);
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, GitBackupUpdate.timeoutUnit);
				addLog(r.getStdoutString());
				addError(r.getStderrString());
				addExitCode("Fetch exit code: ", r);
			}
			{
				// Read all refs (tags, branches, etc) existing in git database
				Process p=Runtime.getRuntime().exec("git show-ref", null, f);
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, GitBackupUpdate.timeoutUnit);
				String refss=r.getStdoutString();
				addExitCode("Get refs: ", r);
				addError(r.getStderrString());
				List<String> lines=UtilString.split(refss, "\r\n");
				for(String line: lines)
				{
					int idx=line.indexOf(' ');
					String code=line.substring(0, idx);
					String url=line.substring(idx+1);
					refNameToHash.put(url, code);
				}
			}
			for(String ref:refNameToHash.keySet())
			{
				if(ref.startsWith(backupRefPrefix))
				{
					// Parse backup prefixes
					String subs=ref.substring(backupRefPrefix.length());
					{
						if(subs.startsWith(a.until))
						{
							String d=subs.substring(a.until.length());
							long t=parseDate(d);
							Pair<String, String> key=parseKey(d, ref);
							addTimeStamp(byUrlHashUntil, key, t);
							existsByBackup.remove(key);
						}
						else if(subs.startsWith(a.from))
						{
							String d=subs.substring(a.from.length());
							long t=parseDate(d);
							Pair<String, String> key=parseKey(d, ref);
							addTimeStamp(byUrlHashFrom, key, t);
							existsByBackup.add(key);
						}else
						{
							// "old style" - previous version backup log
							long t=parseDate(subs);
							allKnowDates.add(t);
							Pair<String, String> key=parseKey(subs, ref);
							TreeSet<Long> datesAtSeen=byUrlHashDates.get(key);
							if(datesAtSeen==null)
							{
								datesAtSeen=new TreeSet<>();
								byUrlHashDates.put(key, datesAtSeen);
							}
							datesAtSeen.add(t);
							oldRefsToDelete.add(ref);
						}
					}
				}
			}
			for(Pair<String, String> key: byUrlHashDates.keySet())
			{
				TreeSet<Long> seenAt=byUrlHashDates.get(key);
				boolean known=false;
				for(long t: allKnowDates)
				{
					if(seenAt.contains(t))
					{
						if(known)
						{
							// Nothing to do
						}else
						{
							createBackupReference(f, a.from, new Date(t), key.getA(), key.getB());
							known=true;
							addTimeStamp(byUrlHashFrom, key, t);
						}
					}else
					{
						if(!known)
						{
							// Nothing to do
						}else
						{
							createBackupReference(f, a.until, new Date(t), key.getA(), key.getB());
							addTimeStamp(byUrlHashUntil, key, t);
							known=false;
						}
					}
				}
				if(known)
				{
					existsByBackup.add(key);
				}
			}
			deleteOldRefsAfterUpgrade();
			// Ref names that exist in the backup but does not exist in the current state of the repo
			HashSet<Pair<String, String>> toDelete=new HashSet<>(existsByBackup);
			for(String ref:refNameToHash.keySet())
			{
				if(!ref.startsWith(backupRefPrefix))
				{
					String code=refNameToHash.get(ref);
					Pair<String, String> key=new Pair<>(ref, code);
					toDelete.remove(key);
					boolean currentlyExists=false;
					if(byUrlHashFrom.get(key)!=null)
					{
						long lastFrom=byUrlHashFrom.get(key).last();
						currentlyExists=true;
						if(byUrlHashUntil.get(key)!=null)
						{
							long lastUntil=byUrlHashUntil.get(key).last();
							if(lastUntil>lastFrom)
							{
								currentlyExists=false;
							}
						}
					}
					if(!currentlyExists)
					{
						// System.out.println("Create REF! "+ref+" "+code);
						createBackupReference(f, a.from, c.getTime(), ref, code);
						getCommitLogMessage(f, code);
					}
				}
			}
			for(Pair<String,String> k: toDelete)
			{
				createBackupReference(f, a.until, c.getTime(), k.getA(), k.getB());
			}
		}finally
		{
			write("....\n");
			writeObject(error?"ERROR":"OK");
			write("\n\n");
		}
	}
	private void deleteOldRefsAfterUpgrade() throws Exception {
		if(!error)
		{
			if(oldRefsToDelete.size()>0)
			{
				write("After Upgrade of old style backup references delete the old references!");
				for(String ref: oldRefsToDelete)
				{
					List<String> cmd0=Arrays.asList("git", "update-ref", "-d", ref);
					execCmd(cmd0, "Delete tag error: ");
				}
			}
		}
	}
	private void createBackupReference(File f, String type, Date date, String refName, String target) throws Exception {
		String src=backupRefPrefix+type+df.format(date)+"/"+refName;
		List<String> cmd0=Arrays.asList("git", "update-ref", src, target);
		execCmd(cmd0, "Create tag error: ");
		hasChange=true;
	}
	private void execCmd(List<String> cmd0, String errPrefix) throws Exception {
		addLog(" $ "+UtilString.concat(cmd0, " "));
		if(!a.debugNoChangeRefs)
		{
			Process p=new ProcessBuilder(cmd0).directory(f).start();
			ProcessFuture pr=UtilProcess2.execute(p);
			ProcessResult r=pr.get(a.timeoutMillis, GitBackupUpdate.timeoutUnit);
			int ec=r.getExitCode();
			//		addLog(r.getStdoutString());
			//		addError(r.getStderrString());
			if(ec!=0)
			{
				error=true;
				addError(errPrefix+"'"+UtilString.concat(cmd0, " ")+"' "+ec);
			}
		}
	}
	private void getCommitLogMessage(File f, String code) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		String cmd0="git log --format=%B  -n 1 "+code;
		Process p=Runtime.getRuntime().exec(cmd0, null, f);
		ProcessFuture pr=UtilProcess2.execute(p);
		ProcessResult r=pr.get(a.timeoutMillis, GitBackupUpdate.timeoutUnit);
		int ec=r.getExitCode();
		String comment=r.getStdoutString();
		if(comment.length()>1024)
		{
			comment=comment.substring(0, 1024)+"...";
		}
		addLog(comment);
		if(ec!=0)
		{
			error=true;
			addError("Get commit log message error: '"+cmd0+"' "+ec);
		}
	}
	private void addTimeStamp(Map<Pair<String, String>, TreeSet<Long>> byUrlHashToTree, Pair<String, String> key,
			long timestamp) {
		TreeSet<Long> ts=byUrlHashToTree.get(key);
		if(ts==null)
		{
			ts=new TreeSet<>();
			byUrlHashToTree.put(key, ts);
		}
		ts.add(timestamp);
	}
	private Pair<String, String> parseKey(String subs, String ref) {
		String u=subs.substring(dateformatTemplate.length()+1);
		Pair<String, String> key=new Pair<String, String>(u, refNameToHash.get(ref));
		return key;
	}
	protected void write(String s)
	{
		// System.out.print(s);
		t.writeDelegate(s);
	}
	protected void writeObject(Object o)
	{
		if(o!=null)
		{
			write(o.toString());
		}else
		{
			write("null");
		}
	}
	public boolean hasError() {
		return error;
	}
	public String getCurrentUrl() {
		return currentUrl;
	}
	private long parseDate(String subs) throws ParseException {
		Date d=df.parse(subs);
		return d.getTime();
	}
	private void addError(String string) {
		if(string.length()==0)
		{
			return;
		}
		writeObject(string);
		write("\n");
	}

	private void addLog(String string) {
		if(string.length()==0)
		{
			return;
		}
		writeObject(string);
		write("\n");
	}
	private void addExitCode(String string, ProcessResult r) throws InterruptedException {
		int code=r.getExitCode();
		if(code!=0)
		{
			error=true;
		}
		addLog(string+code+(code!=0?" ERROR":""));
	}
	public boolean hasChange() {
		return hasChange;
	}
	public String getOutput()
	{
		return t.getResult();
	}
}
