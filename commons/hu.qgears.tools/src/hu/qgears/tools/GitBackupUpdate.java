package hu.qgears.tools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
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
		@JOHelp("Prefix of the backup tags created by the backup tool. (Also tags whose name start with this are considered backup tags and are not backed up again.)")
		public String backupTagPrefix="backup-";
		@JOHelp("Salt to the tag names so they can not coincidentally match remote names and be overwritten.")
		public String salt="-"+new Random().nextLong();
		@JOHelp("Commit latest backup date to this file. The file must be in a git repo branch which is only written by this client (commit and push only this single file). (When not present log is not saved.)")
		public File backupLogFile;
		public void validate() {
			if(repos==null||!repos.isDirectory()||!repos.exists())
			{
				throw new IllegalArgumentException("repos must be an existing folder.");
			}
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
				write("Freshen git backups to the latest version. Create a backup tag of all tags and branches.\n\nReturn value: Returns non 0 in case any of the gits could not be fetched or the log could not be uploaded.\n\nLogging:\n\n * If backupLogFile is specified: then the result log is pushed into a git repository (in which the log file resides).\n * If backupLogFile is not specified: Logs are written to stdout\n  \nProposed configuration is to start backup by timer (eg. daily) and restart the process (eg. every hour) in case the of non 0 return value.\n\n");
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
		try {
			for(File f:UtilFile.listFiles(a.repos))
			{
				try {
					if(!f.getName().startsWith(".") && f.isDirectory())
					{
						currentUrl=null;
						boolean errorState=error;
						error=false;
						write("\n== ");
						DeferredTemplate errTempl=t.deferredDelegate(this::writeError);//NB
						write(": ");
						writeObject(f.getName());
						DeferredTemplate fromTempl=t.deferredDelegate(this::writeFrom);//NB
						write("\n\n....\n");
						try
						{
							{
								Process p=Runtime.getRuntime().exec("git remote get-url origin", null, f);
								ProcessFuture pr=UtilProcess2.execute(p);
								currentUrl=pr.get(a.timeoutMillis, timeoutUnit).getStdoutString();
								fromTempl.generate();
							}
							{
								Process p=Runtime.getRuntime().exec("git fetch", null, f);
								ProcessFuture pr=UtilProcess2.execute(p);
								ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
								addLog(r.getStdoutString());
								addError(r.getStderrString());
								addExitCode("Fetch exit code: ", r);
							}
							List<String> tags=new ArrayList<>(); 
							{
								Process p=Runtime.getRuntime().exec("git tag --list", null, f);
								ProcessFuture pr=UtilProcess2.execute(p);
								ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
								String tagss=r.getStdoutString();
								addExitCode("Get tags: ", r);
								addError(r.getStderrString());
								tags.addAll(UtilString.split(tagss, "\r\n"));
							}
							List<String> branches=new ArrayList<>(); 
							{
								Process p=Runtime.getRuntime().exec("git branch --list", null, f);
								ProcessFuture pr=UtilProcess2.execute(p);
								ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
								String branchess=r.getStdoutString();
								addExitCode("Get Branches: ", r);
								addError(r.getStderrString());
								branches.addAll(UtilString.split(branchess, "\r\n"));
								for(int i=0;i<branches.size();++i)
								{
									String b=branches.get(i);
									b=b.substring(2);
									branches.set(i, b);
								}
							}
							for(String tag: tags)
							{
								if(!tag.startsWith(a.backupTagPrefix))
								{
									createTag(f, tag, a.backupTagPrefix+df.format(c.getTime()), "-tag-"+tag);
								}
							}
							for(String branch: branches)
							{
								createTag(f, branch, a.backupTagPrefix+df.format(c.getTime()), "-branch-"+branch);
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
	
	private void writeError(Object o)
	{
		writeObject(error?"ERROR":"OK");
	}
	private void writeFrom(Object o)
	{
		write(" from: ");
		writeObject(currentUrl);
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

	private void createTag(File f, String src, String targetPre, String targetPost) throws Exception {
		String cmd0="git tag "+targetPre+targetPost+" "+src;
		addLog(" $ "+cmd0);
		String cmd="git tag "+targetPre+a.salt+targetPost+" "+src;
		Process p=Runtime.getRuntime().exec(cmd, null, f);
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
