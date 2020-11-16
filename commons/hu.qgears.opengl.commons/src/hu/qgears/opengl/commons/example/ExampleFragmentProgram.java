package hu.qgears.opengl.commons.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBFragmentProgram;
import org.lwjgl.opengl.ARBProgram;
import org.lwjgl.opengl.GL11;

import hu.qgears.commons.UtilFile;
import hu.qgears.opengl.commons.AbstractOpenglApplication2;
import hu.qgears.opengl.commons.UtilGl;

/**
 * TODO finish the example
 * @author rizsi
 *
 */
public class ExampleFragmentProgram extends AbstractOpenglApplication2{

	private static final Logger LOG = LogManager.getLogger(
			ExampleFragmentProgram.class);
	
	public static void main(String[] args) throws Exception {
		new ExampleFragmentProgram().execute();
	}
	@Override
	protected boolean isDirty() {
		return true;
	}
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		int program=ARBFragmentProgram.glGenProgramsARB();
		if(program==0)
		{
			throw new RuntimeException("Can not initialize fragment program");
		}
		ARBFragmentProgram.glBindProgramARB(ARBFragmentProgram.GL_FRAGMENT_PROGRAM_ARB, program);
		String prog=UtilFile.loadAsString(getClass().getResource("program00Fragment.gl"));
		ARBFragmentProgram.glProgramStringARB(program, ARBProgram.GL_PROGRAM_FORMAT_ASCII_ARB, prog);
		if(!ARBFragmentProgram.glIsProgramARB(program)) {
			String err=GL11.glGetString(ARBFragmentProgram.GL_PROGRAM_ERROR_STRING_ARB);
			LOG.info(""+prog+" "+err);
		}
	}
	@Override
	protected void render() {
		UtilGl.drawMinimalScene();
	}

	@Override
	protected void logic() {
		//nothing to do
	}
	@Override
	protected void logError(String message, Exception e) {
		LOG.error(message,e);
	}
	
}
