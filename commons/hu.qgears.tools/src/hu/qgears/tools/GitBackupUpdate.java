package hu.qgears.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.rtemplate.runtime.Consumer;
import hu.qgears.rtemplate.runtime.DeferredTemplate;
import hu.qgears.rtemplate.runtime.RQuickTemplate;
import hu.qgears.tools.UtilProcess2.ProcessFuture;
import hu.qgears.tools.UtilProcess2.ProcessResult;
import joptsimple.annot.JOHelp;

public class GitBackupUpdate extends AbstractTool
{
	private TemplateDelegate t=new TemplateDelegate();
	public class Args implements IArgs{
		@JOHelp("Repository backup folder - subfolders are repository backups. All are fetched and then all tags and branches are tagged (except the tags that are already local backups) so that they are not overwritten with next fetches.")
		public File repos;
		@JOHelp("Timeout before process is considered dead. This timeout includes time to download all objects which may be much time in case of slow network conenction.")
		public long timeoutMillis=60000*30;
		@JOHelp("Commit latest backup date to this file. The file must be in a git repo branch which is only written by this client (commit and push only this single file). (When not present log is not saved.)")
		public File backupLogFile;
		@JOHelp("Pointer to file of URLs to be backed up. Paramter of the 'git show' command. Containing repo must be within the backed up repositories within the repos folder. Must be $repofolder$:$branch$:$path$ Example: 'master:backupConfig.txt'. If this argument is present then the specified branch is fetched from origin first then the config is parsed.")
		public String backupConfig;
		@JOHelp("Reference name to be used for backups. Backup refs are generated like: /refs/$backupRef$/date/original")
		public String backupRef="backup";
		@JOHelp("refs to be backed up. Default: heads, tags means: git fetch -p origin +refs/heads/*:refs/heads/* +refs/tags/*:refs/tags/*")
		public List<String> refs=createDefaultRefsList();
		public void validate() {
			if(repos==null||!repos.isDirectory()||!repos.exists())
			{
				throw new IllegalArgumentException("repos must be an existing folder.");
			}
			validateRef(backupRef, "backup ref");
			if(refs.size()<1)
			{
				throw new IllegalArgumentException("refs must not be empty");
			}
			for(String r:refs)
			{
				validateRef(r, "ref to be backed up");
				if(backupRef.equals(r))
				{
					if(r.contains("/"))
					{
						throw new IllegalArgumentException("ref to be backed up must not be equal to the backup reference.");
					}
				}
			}
		}
		private void validateRef(String r, String string) {
			if(r.contains("/"))
			{
				throw new IllegalArgumentException(string+" must not contain '/'");
			}
			if(r.contains(","))
			{
				throw new IllegalArgumentException(string+" must not contain ','");
			}
		}
		private List<String> createDefaultRefsList() {
			List<String> ret=new ArrayList<>();
			ret.add("heads");
			ret.add("tags");
			return ret;
		}
	}
	private TimeUnit timeoutUnit=TimeUnit.MILLISECONDS;

	private SimpleDateFormat df=new SimpleDateFormat("yyyy.MM.dd.HH-mm-ss");
	private Args a;
	private boolean error;
	private String currentUrl;

	@Override
	public String getId() {
		return "git-backup";
	}

	@Override
	public String getDescription() {
		return new RQuickTemplate() {
			
			@Override
			protected void doGenerate() {
				write("Freshen git backups to the latest version. Create a backup tag of all tags and branches.\n\nReturn value: Returns non 0 in case any of the gits could not be fetched or the log could not be uploaded.\n\nConfiguration of the backup repo: 'git clone --bare $URL' is enough. refs to be backed up are specified manually by the --refs argument.\nConfiguring to fetch /refs/*:/refs/* is discouraged because you can accidentally overwrite the backups below /refs/backup/\n\nFetch is done with the command: git fetch -p origin +refs/heads/*:refs/heads/* +refs/tags/*:refs/tags/*\nThis means that remote deleted refs are deleted (in ref name spaces that are backed up. So the backup namespace must not be used here!)\n\n\nLogging:\n\n * If backupLogFile is specified: then the result log is pushed into a git repository (in which the log file resides).\n * If backupLogFile is not specified: Logs are written to stdout\n  \nProposed configuration is to start backup by timer (eg. daily) and restart the process (eg. every hour) in case the of non 0 return value.\n\n");
			}
		}.generate();
	}
	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}
	@Override
	public int doExec(IArgs args) throws Exception {
		Calendar c=Calendar.getInstance();
		a=(Args) args;
		Set<String> reqUrls=null;
		if(a.backupConfig!=null)
		{
			write("== Update backupConfig file\n\n....\n");
			try
			{
				write("Config: ");
				writeObject(a.backupConfig);
				write("\n");
				List<String> pieces=UtilString.split(a.backupConfig, ":");
				String folder=pieces.get(0);
				File f=new File(a.repos, folder);
				String branch=pieces.get(1);
				String path=pieces.get(2);
				String cmd="git fetch origin +refs/heads/"+branch+":refs/heads/"+branch;
				Process p=Runtime.getRuntime().exec(cmd, null, f);
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
				addLog(" $ "+cmd);
				if(r.getExitCode()!=0)
				{
					addExitCode("Update repo containing config: ", r);
					addLog(r.getStdoutString());
					addError(r.getStderrString());
				}
				cmd="git show "+branch+":"+path;
				write(" $ ");
				writeObject(cmd);
				write("\n");
				p=Runtime.getRuntime().exec(cmd, null, f);
				pr=UtilProcess2.execute(p);
				r=pr.get(a.timeoutMillis, timeoutUnit);
				if(r.getExitCode()!=0)
				{
					addExitCode("Read configuration from repo: ", r);
					addLog(""+cmd);
					addLog(r.getStdoutString());
					addError(r.getStderrString());
				}
				reqUrls=new TreeSet<>();
				for(String s: UtilString.split(r.getStdoutString(), "\r\n"))
				{
					String trimmed=s.trim();
					if(trimmed.length()>0 && !trimmed.startsWith("#"))
					{
						reqUrls.add(trimmed);
					}
				}
			}finally
			{
				write("....\n");
			}
		}
		Set<String> updatedUrls=new TreeSet<>();
		try {
			Map<String, String> urls=new HashMap<>();
			for(File f:UtilFile.listFiles(a.repos))
			{
				try {
					if(!f.getName().startsWith(".") && f.isDirectory())
					{
						{
							Process p=Runtime.getRuntime().exec("git remote get-url origin", null, f);
							ProcessFuture pr=UtilProcess2.execute(p);
							String url=pr.get(a.timeoutMillis, timeoutUnit).getStdoutString();
							urls.put(UtilString.split(url, "\r\n").get(0), f.getAbsolutePath());
						}
					}
				}catch(Exception e)
				{
					addError(e);
				}
			}
			if(reqUrls!=null)
			{
				for(String s: reqUrls)
				{
					if(!urls.containsKey(s))
					{
						write("== Clone: ");
						writeObject(s);
						write("\n\n....\n");
						try
						{
							String cmd="git clone --bare "+s;
							Process p=Runtime.getRuntime().exec(cmd, null, a.repos);
							addLog(" $ "+cmd);
							ProcessFuture pf=UtilProcess2.execute(p);
							ProcessResult pr=pf.get(a.timeoutMillis, timeoutUnit);
							addExitCode("clone url "+s+": ", pr);
							if(pr.getExitCode()!=0)
							{
								addLog(pr.getStdoutString());
								addError(pr.getStderrString());
							}
						}finally
						{
							write("....\n");
						}
					}
				}
			}
			for(File f:UtilFile.listFiles(a.repos))
			{
				try {
					if(!f.getName().startsWith(".") && f.isDirectory())
					{
						currentUrl=null;
						boolean errorState=error;
						error=false;
						write("\n== ");
						DeferredTemplate errTempl=t.deferredDelegate(new WriteError());//NB
						write(": ");
						writeObject(f.getName());
						DeferredTemplate fromTempl=t.deferredDelegate(new WriteFrom());//NB
						write("\n\n....\n");
						try
						{
							{
								Process p=Runtime.getRuntime().exec("git remote get-url origin", null, f);
								ProcessFuture pr=UtilProcess2.execute(p);
								currentUrl=UtilString.split(pr.get(a.timeoutMillis, timeoutUnit).getStdoutString(), "\r\n").get(0);
								updatedUrls.add(currentUrl);
								fromTempl.generate();
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
								ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
								addLog(r.getStdoutString());
								addError(r.getStderrString());
								addExitCode("Fetch exit code: ", r);
							}
							Map<String, String> refs=new TreeMap<>();
							{
								Process p=Runtime.getRuntime().exec("git show-ref", null, f);
								ProcessFuture pr=UtilProcess2.execute(p);
								ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
								String refss=r.getStdoutString();
								addExitCode("Get refs: ", r);
								addError(r.getStderrString());
								List<String> lines=UtilString.split(refss, "\r\n");
								for(String line: lines)
								{
									int idx=line.indexOf(' ');
									String code=line.substring(0, idx);
									String url=line.substring(idx+1);
									refs.put(url, code);
								}
							}
							String backupRefPrefix="refs/"+a.backupRef+"/";
							for(String ref:refs.keySet())
							{
								if(!ref.startsWith(backupRefPrefix))
								{
									String code=refs.get(ref);
									createBackup(f, code, backupRefPrefix+df.format(c.getTime())+"/"+ref);
									getCommitLogMessage(f, code);
								}
							}
						}finally
						{
							write("....\n");
							errTempl.generate();
							error=error|errorState;
							write("\n\n");
						}
					}
				} catch (Exception e) {
					addError(e);
				}
			}
		} catch (Exception e) {
			addError(e);
		}
		if(reqUrls!=null)
		{
			for(String s: updatedUrls)
			{
				if(!reqUrls.contains(s))
				{
					write("\n== Unnecessary backup: '");
					writeObject(s);
					write("'\n\n"+reqUrls);
				}
			}
		}
		{
			write("== Disk usage\n\n");
			Process p=Runtime.getRuntime().exec("du -sh .", null, a.repos);
			ProcessFuture pr=UtilProcess2.execute(p);
			ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
			if(r.getExitCode()!=0)
			{
				addExitCode("Get bytes used: ", r);
				addLog(r.getStdoutString());
				addError(r.getStderrString());
			}else
			{
				addLog("Backup folder uses bytes on disk: "+r.getStdoutString());
			}
		}
		if(a.backupLogFile!=null)
		{
			String log=t.getResult();
			t=new TemplateDelegate();
			write("= Backup of Git repos\n\nDate: ");
			writeObject(df.format(c.getTime()));
			write("\nResult: ");
			writeObject((error?"ERROR":"OK"));
			write("\n\n\n");
			writeObject(log);
			a.backupLogFile.getParentFile().mkdirs();
			UtilFile.saveAsFile(a.backupLogFile, t.getResult());
			{
				String[] cmdarray=new String[]{"git", "add", "."};
				Process p=Runtime.getRuntime().exec(cmdarray, null, a.backupLogFile.getParentFile());
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
				addExitCode("Log git add results: ", r);
				addLog(r.getStdoutString());
				addError(r.getStderrString());
			}
			{
				String[] cmdarray=new String[]{"git", "commit", "-m", "Autobackup: "+df.format(c.getTime())};
				Process p=Runtime.getRuntime().exec(cmdarray, null, a.backupLogFile.getParentFile());
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
				addExitCode("Commit log results: ", r);
				addLog(r.getStdoutString());
				addError(r.getStderrString());
			}
			{
				Process p=Runtime.getRuntime().exec("git push", null, a.backupLogFile.getParentFile());
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
				addExitCode("Push log results: ", r);
				addLog(r.getStdoutString());
				addError(r.getStderrString());
			}
		}
		return error?1:0;
	}
	
	class WriteError implements Consumer<Object[]>
	{
		@Override
		public void accept(Object[] param) {
			writeObject(error?"ERROR":"OK");
		}
	}
	
	class WriteFrom implements Consumer<Object[]>
	{
		@Override
		public void accept(Object[] param) {
			write(" from: ");
			writeObject(currentUrl);
		}
	}

	private void addError(Exception e) {
		error=true;
		e.printStackTrace();
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw); 
		e.printStackTrace(pw);
		pw.flush();
		writeObject(sw.toString());
		write("\n\n");
	}

	private void addExitCode(String string, ProcessResult r) throws InterruptedException {
		int code=r.getExitCode();
		if(code!=0)
		{
			error=true;
		}
		addLog(string+code+(code!=0?" ERROR":""));
	}

	private void createBackup(File f, String src, String target) throws Exception {
		String cmd0="git update-ref "+target+" "+src;
		addLog(" $ "+cmd0);
		Process p=Runtime.getRuntime().exec(cmd0, null, f);
		ProcessFuture pr=UtilProcess2.execute(p);
		ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
		int ec=r.getExitCode();
		//		addLog(r.getStdoutString());
		//		addError(r.getStderrString());
		if(ec!=0)
		{
			error=true;
			addError("Create tag error: '"+cmd0+"' "+ec);
		}
	}
	private void getCommitLogMessage(File f, String code) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		String cmd0="git log --format=%B  -n 1 "+code;
		addLog(" $ "+cmd0);
		Process p=Runtime.getRuntime().exec(cmd0, null, f);
		ProcessFuture pr=UtilProcess2.execute(p);
		ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
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
	protected void write(String s)
	{
		System.out.print(s);
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
}
