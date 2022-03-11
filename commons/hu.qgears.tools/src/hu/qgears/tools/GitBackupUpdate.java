package hu.qgears.tools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilString;
import hu.qgears.rtemplate.runtime.Consumer;
import hu.qgears.rtemplate.runtime.RQuickTemplate;
import hu.qgears.tools.UtilProcess2.ProcessFuture;
import hu.qgears.tools.UtilProcess2.ProcessResult;
import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;
import joptsimple.annot.JOSkip;
import joptsimple.tool.AbstractTool;

public class GitBackupUpdate extends AbstractTool
{
	private Calendar c;
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
		@JOHelp("Debug feature: only list the folders then print status and exit!")
		@JOSimpleBoolean
		public boolean debugExitAfterListExistingFolders=false;
		@JOHelp("Debug feature: exit after cloning new repo folders")
		@JOSimpleBoolean
		public boolean debugExitAfterClone=false;
		@JOHelp("Debug feature: exit without updating the log file")
		@JOSimpleBoolean
		public boolean debugExitBeforeBackupLog=false;
		@JOHelp("Debug feature: exit after first updated backup folder")
		@JOSimpleBoolean
		public boolean debugExitAfterFirstUpdated=false;
		@JOHelp("Debug feature: do not create backup refs but only log the commands would be executed.")
		@JOSimpleBoolean
		public boolean debugNoChangeRefs=false;
		@JOSkip
		public String from="from/";
		@JOSkip
		public String until="until/";
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
	public final static TimeUnit timeoutUnit=TimeUnit.MILLISECONDS;

	private SimpleDateFormat df=new SimpleDateFormat("yyyy.MM.dd.HH-mm-ss");
	private Args a;
	private boolean error;
	private Set<String> reqUrls=null;
	private Set<String> updatedUrls=new TreeSet<>();
	/**
	 * Maps URL to folder absolute path name.
	 */
	private Map<String, String> urls=new HashMap<>();

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
		c=Calendar.getInstance();
		a=(Args) args;
		readBackupConfig();
		try {
			listExistingBackups();
			if(a.debugExitAfterListExistingFolders)
			{
				return debugExitAfterListExistingFolders();
			}
			executeClones();
			if(a.debugExitAfterClone)
			{
				write("debugExitAfterClone\n");
				return 0;
			}
			updateAllBackupFolders();
		} catch (Exception e) {
			addError(e);
		}
		printAllUpdated();
		if(a.debugExitAfterFirstUpdated)
		{
			write("debugExitAfterFirstUpdated\n");
			return 0;
		}
		printUnnecessaryBackups();
		printDiskUsage();
		if(a.debugExitBeforeBackupLog)
		{
			write("debugExitBeforeBackupLog\n");
			return 0;
		}
		commitBackupLogFile();
		return error?1:0;
	}
	
	private void printAllUpdated() {
		write("= All updated\n\n");
		UtilComma comma=new UtilComma(", ");
		for(String s: updatedUrls)
		{
			writeObject(comma.getSeparator());
			writeObject(s);
		}
		write("\n\n");
	}

	private void commitBackupLogFile() throws Exception {
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
	}

	private void printDiskUsage() throws Exception {
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
		p=Runtime.getRuntime().exec("df -h .", null, a.repos);
		pr=UtilProcess2.execute(p);
		r=pr.get(a.timeoutMillis, timeoutUnit);
		if(r.getExitCode()!=0)
		{
			addExitCode("Get available space: ", r);
			addLog(r.getStdoutString());
			addError(r.getStderrString());
		}else
		{
			addLog("Backup folder available space on disk: "+r.getStdoutString());
		}
	}

	private void printUnnecessaryBackups() {
		if(reqUrls!=null)
		{
			Set<String> unnecessary=new TreeSet<>();
			for(String s: updatedUrls)
			{
				if(!reqUrls.contains(s))
				{
					unnecessary.add(s);
				}
			}
			if(unnecessary.size()>0)
			{
				write("\n== Unnecessary backup: '");
				writeObject(unnecessary);
				write("'\n\n");
			}
		}
	}

	private void updateAllBackupFolders() {
		for(File f:UtilFile.listFiles(a.repos))
		{
			try {
				if(!f.getName().startsWith(".") && f.isDirectory())
				{
					BackupSingleGitRepo b=new BackupSingleGitRepo(f, a, c);
					b.backup();
					if(b.hasChange() || b.hasError())
					{
						t.writeDelegate(b.getOutput());
					}
					updatedUrls.add(b.getCurrentUrl());
					error|=b.hasError();
				}
			} catch (Exception e) {
				addError(e);
			}
			if(a.debugExitAfterFirstUpdated)
			{
				return;
			}
		}
	}

	private void executeClones() throws Exception {
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
						List<String> cmd=Arrays.asList("git", "clone", "--bare", s);
						Process p=new ProcessBuilder(cmd).directory(a.repos).start();
						addLog(" $ "+UtilString.concat(cmd, " "));
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
	}

	private int debugExitAfterListExistingFolders() {
		for(String key: urls.keySet())
		{
			System.out.println(key+" -> "+urls.get(key));
		}
		for(String s: reqUrls)
		{
			if(!urls.containsKey(s))
			{
				System.out.println("REQ CLONE: "+s);
			}
		}
		return 0;
	}

	/**
	 * List all sub-folders in the backup folder
	 * and check which folders back up which git reference.
	 * Fills the urls field.
	 */
	private void listExistingBackups() {
		for(File f:UtilFile.listFiles(a.repos))
		{
			try {
				if(!f.getName().startsWith(".") && f.isDirectory())
				{
					{
						List<String> cmd=Arrays.asList("git", "remote", "get-url", "origin");
						Process p=new ProcessBuilder(cmd).directory(f).start();
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
	}
	/**
	 * Update from git and load the backup configuration file.
	 * @throws Exception
	 */
	private void readBackupConfig() throws Exception {
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
				List<String> cmd=Arrays.asList("git", "fetch", "origin", "+refs/heads/"+branch+":refs/heads/"+branch);
				Process p=new ProcessBuilder(cmd).directory(f).start();
				ProcessFuture pr=UtilProcess2.execute(p);
				ProcessResult r=pr.get(a.timeoutMillis, timeoutUnit);
				addLog(" $ "+UtilString.concat(cmd, " "));
				if(r.getExitCode()!=0)
				{
					addExitCode("Update repo containing config: ", r);
					addLog(r.getStdoutString());
					addError(r.getStderrString());
				}
				cmd=Arrays.asList("git", "show", branch+":"+path);
				write(" $ ");
				writeObject(UtilString.concat(cmd, " "));
				write("\n");
				p=new ProcessBuilder(cmd).directory(f).start();
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
	}

	class WriteError implements Consumer<Object[]>
	{
		@Override
		public void accept(Object[] param) {
			writeObject(error?"ERROR":"OK");
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
