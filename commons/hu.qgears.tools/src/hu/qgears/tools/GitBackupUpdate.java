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
import hu.qgears.tools.UtilProcess2.ProcessFuture;
import hu.qgears.tools.UtilProcess2.ProcessResult;
import joptsimple.annot.JOHelp;

public class GitBackupUpdate extends AbstractTool
{
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

	private StringBuilder log=new StringBuilder();
	private SimpleDateFormat df=new SimpleDateFormat("yyyy.MM.dd.HH-mm-ss");
	private Args a;
	private boolean error;

	@Override
	public String getId() {
		return "git-backup";
	}

	@Override
	public String getDescription() {
		return "Freshen git backups to the latest version. Create a backup tag of all tags and branches.";
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
						String url; 
						{
							Process p=Runtime.getRuntime().exec("git remote get-url origin", null, f);
							ProcessFuture pr=UtilProcess2.execute(p);
							url=pr.get(a.timeoutMillis, timeoutUnit).getStdoutString();
						}
						{
							Process p=Runtime.getRuntime().exec("git fetch", null, f);
							ProcessFuture pr=UtilProcess2.execute(p);
							ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
							addLog("Updating: "+f.getName()+"from: "+url);
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
			StringBuilder w=new StringBuilder();
			w.append("Date: "+df.format(c.getTime())+"\n");
			w.append("Result: "+(error?"ERROR":"OK")+"\n\n");
			w.append(log);
			UtilFile.saveAsFile(a.backupLogFile, w.toString());
			{
				String[] cmdarray=new String[]{"git", "commit", "-am", "Autobackup: "+df.format(c.getTime())};
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

	private void addError(Exception e) {
		error=true;
		e.printStackTrace();
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw); 
		e.printStackTrace(pw);
		pw.flush();
		log.append(sw.toString());
		log.append("\n");
	}

	private void addExitCode(String string, ProcessResult r) throws InterruptedException {
		int code=r.getExitCode();
		if(code!=0)
		{
			error=true;
		}
		addLog(string+code);
	}

	private void createTag(File f, String src, String targetPre, String targetPost) throws Exception {
		String cmd0="git tag "+targetPre+targetPost+" "+src;
		addLog("$ "+cmd0);
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
		System.err.println(string);
		log.append(string);
		log.append("\n");
	}

	private void addLog(String string) {
		if(string.length()==0)
		{
			return;
		}
		System.out.println(string);
		log.append(string);
		log.append("\n");
	}
}
