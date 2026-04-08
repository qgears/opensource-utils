package hu.qgears.hu.qgears.jacoco.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.xml.XMLFormatter;

public class JacocoReportGenerator {

	private String title = "My jacoco report";
	private final List<File> executionDataFiles = new ArrayList<>();
	private final List<File> classesDirectories = new ArrayList<>();
	private final List<File> sourceDirectories = new ArrayList<>();
	private File xmlReportPath;
	private int tabwidth = 4;
	private ExecFileLoader execFileLoader;
	private Log log;

	public JacocoReportGenerator(Log log) {
		this.log = log;
	}

	/**
	 * Create the report.
	 *
	 * @throws IOException
	 */
	public void create() throws Exception {
		// Read the jacoco.exec file. Multiple data files could be merged
		// at this point
		log.info("Load execution data... ");
		if(executionDataFiles.isEmpty()) {
			log.warn("executionDataFiles is empty (configured jacoco.exec files do not exist");
		}
		loadExecutionData();
		log.info("Init structure ");
		final IBundleCoverage bundleCoverage = analyzeStructure();
		log.info("Create report ");
		createReport(bundleCoverage);
	}

	private void createReport(final IBundleCoverage bundleCoverage) throws IOException {

		// configuration. In this case we use the defaults
		final XMLFormatter htmlFormatter = new XMLFormatter();
		if (!xmlReportPath.getParentFile().exists()) {
			if (!xmlReportPath.getParentFile().mkdirs()) {
				throw new IOException("Cannot create folder " + xmlReportPath.getAbsolutePath());
			}
		}
		final IReportVisitor visitor = htmlFormatter.createVisitor(new FileOutputStream(xmlReportPath));

		// Initialize the report with all of the execution and session
		// information. At this point the report doesn't know about the
		// structure of the report being created
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents());

		// Populate the report structure with the bundle coverage information.
		// Call visitGroup if you need groups in your report.
		visitor.visitBundle(bundleCoverage, getSourceLocator());

		// Signal end of structure information to allow report to write all
		// information out
		visitor.visitEnd();

	}

	private void loadExecutionData() throws IOException {
		execFileLoader = new ExecFileLoader();
		for (File executionDataFile : executionDataFiles) {
			execFileLoader.load(executionDataFile);
		}
	}

	private IBundleCoverage analyzeStructure() throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);
		for (File classesDirectory : classesDirectories) {
			analyzer.analyzeAll(classesDirectory);
		}
		return coverageBuilder.getBundle(title);
	}

	private ISourceFileLocator getSourceLocator() {
		final MultiSourceFileLocator multi = new MultiSourceFileLocator(tabwidth);
		for (final File f : sourceDirectories) {
			multi.add(new DirectorySourceFileLocator(f, "UTF-8", tabwidth));
		}
		return multi;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setXmlReportPath(File xmlReportPath) {
		this.xmlReportPath = xmlReportPath;
	}

	public void setTabwidth(int tabwidth) {
		this.tabwidth = tabwidth;
	}

	public void addClassPath(File dir) {
		if (dir.exists()) {
			classesDirectories.add(dir);
		} else {
			log.info("Ignore non existing class path file "+ dir);
		}
	}

	public void addSourceDir(File dir) {
		if (dir.exists()) {
			sourceDirectories.add(dir);
		} else {
			log.info("Ignore non existing source dir "+ dir);
		}
	}

	public void addExecfile(File execFile) {
		if (execFile.exists()) {
			executionDataFiles.add(execFile);
		} else {
			log.info("Ignore non existing exec file : "+execFile);
		}
	}

}