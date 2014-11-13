package hu.qgears.opengl.commons;

public class FpsCounter {
	private long lastTime;
	private int ctr=0;
	private double fps;
	public double getFps() {
		return fps;
	}
	boolean logToScreen=false;
	public boolean isLogToScreen() {
		return logToScreen;
	}
	public FpsCounter setLogToScreen(boolean logToScreen) {
		this.logToScreen = logToScreen;
		return this;
	}
	public void frameDrawn()
	{
		ctr++;
		long curr=System.nanoTime();
		long elapsed=curr-lastTime;
		if(elapsed>1000000000l||elapsed<0)
		{
			if(elapsed<100000000000l)
			{
				fps=((double)ctr)/elapsed*1000000000.0;
				if(logToScreen)
				{
					System.out.println("FPS: "+fps);
				}
			}
			lastTime=curr;
			ctr=0;
		}
	}
}
