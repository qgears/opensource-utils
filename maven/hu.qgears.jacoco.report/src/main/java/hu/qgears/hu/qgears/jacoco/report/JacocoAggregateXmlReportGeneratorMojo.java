package hu.qgears.hu.qgears.jacoco.report;


import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal that generates a jacoco XML report from a set of exec files, using the class and source directories
 * of the current reactor maven modules (aggregate mode).
 * 
 */
@Mojo( name = "report", defaultPhase = LifecyclePhase.PACKAGE, aggregator = true)
public class JacocoAggregateXmlReportGeneratorMojo
    extends AbstractMojo
{
    /**
     * The title of the XML report.
     */
    @Parameter( defaultValue = "${project.name}", required = true )
    private String title;

    /**
     * The output path, where the XML report will be saved.
     */
    @Parameter( defaultValue = "${project.build.directory}/jacoco.report.xml", required = true )
    private File xmlReportPath;

    /**
     * The exeFile to include in this report
     */
    @Parameter(  required = false)
    private List<File> execFiles;
    /**
     * Used to format source code sections in report.
     */
    @Parameter( defaultValue = "4", required = false)
    private int reportTabWidth = 4;

    /**
     * Ask maven to inject reactor projects
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;
    
    public void execute()
        throws MojoExecutionException
    {
    	checkargs();
    	Log log = getLog();
        try
        {
        	final JacocoReportGenerator generator = new JacocoReportGenerator(getLog());
        	generator.setTitle(title);
        	generator.setTabwidth(reportTabWidth);
        	generator.setXmlReportPath(xmlReportPath);
        	for (File e : execFiles) {
        		if (log.isDebugEnabled()) {
        			log.debug("Feed exec file: "+e);
        		}
        		generator.addExecfile(e);
        	}
        	for (MavenProject reactorProject : reactorProjects) {
        		if (log.isDebugEnabled()) {
        			log.debug("Processing reactor project "+ reactorProject);
        		}
        		File classPathDir = new File(reactorProject.getBuild().getOutputDirectory());
        		generator.addClassPath(classPathDir);
        		if (log.isDebugEnabled()) {
        			log.debug("Class path directory added "+ classPathDir);
        		}
        		
        		for (String srcDir : reactorProject.getCompileSourceRoots()) {
        			File srcDirectory = new File(srcDir);
					generator.addSourceDir(srcDirectory);
        			if (log.isDebugEnabled()) {
        				log.debug("Source directory added "+ srcDirectory);
        			}
        		}
        	}
            generator.create();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error running report generator", e );
        }
    }

	private void checkargs() throws MojoExecutionException {
		if (title == null) {
			throw new MojoExecutionException("Title parameter is not specified "+title);
		}
		if (execFiles == null || execFiles.isEmpty()) {
			throw new MojoExecutionException("execFiles parameter must not be empty");
		} 
	}
}
