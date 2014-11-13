package hu.qgears.opengl.commons.example;

import hu.qgears.commons.UtilFile;
import hu.qgears.opengl.commons.AbstractOpenglApplication2;
import hu.qgears.opengl.commons.UtilGl;

import org.lwjgl.opengl.ARBFragmentProgram;
import org.lwjgl.opengl.ARBProgram;
import org.lwjgl.opengl.GL11;

/**
 * TODO finish the example
 * @author rizsi
 *
 */
public class ExampleFragmentProgram extends AbstractOpenglApplication2{

	public static void main(String[] args) throws Exception {
		new ExampleFragmentProgram().execute();
	}
	@Override
	protected boolean isDirty() {
		return true;
	}
//	Shader shader;
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
			System.out.println(""+prog+" "+err);
//			throw new RuntimeException("Can compile fragment program: "+err);
		}
	}
	@Override
	protected void render() {
//		shader.activate();
		UtilGl.drawMinimalScene();
//		shader.deActivate();
	}

	@Override
	protected void logic() {
		
	}
	@Override
	protected void logError(String message, Exception e) {
		e.printStackTrace();
	}
	
}
