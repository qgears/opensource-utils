package hu.qgears.opengl.commons.context;


import hu.qgears.opengl.commons.IRenderOnTexture;
import hu.qgears.opengl.commons.UtilGl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;


/**
 * All opengl states must be set using this
 * class.
 * 
 * setting a property will trigger an
 * opengl call but only in case that
 * the state is not the same stored locally.
 * 
 * @author rizsi
 *
 */
public final class RGlContext implements Cloneable {
	static class Save
	{
		ECullState cullState=ECullState.off;
		boolean lightEnabled;
		boolean texture2d;
		boolean depthTest;
		EBlendFunc blendFunc=EBlendFunc.off;
		public void save(RGlContext context) {
				lightEnabled=context.lightEnabled;
				texture2d=context.texture2d;
				cullState=context.cullState;
				blendFunc=context.blendFunc;
				depthTest=context.depthTest;
		}
		public void reset(RGlContext context)
		{
			context.lightEnabledReq=lightEnabled;
			context.texture2dReq=texture2d;
			context.cullStateReq=cullState;
			context.blendFuncReq=blendFunc;
			context.depthTestReq=depthTest;
		}
	}
	private List<Save> saves=new ArrayList<Save>();
	private int nSaves=0;
	private ECullState cullState=ECullState.off;
	private ECullState cullStateReq=ECullState.off;
	private boolean lightEnabledReq;
	private boolean lightEnabled;
	private boolean texture2d, texture2dReq;
	private boolean depthTestReq, depthTest;
	private EBlendFunc blendFunc=EBlendFunc.off;
	private EBlendFunc blendFuncReq=EBlendFunc.off;
	private Rectangle defaultViewport=new Rectangle();
	
	public RGlContext()
	{
		
	}
	public RGlContext setCullState(ECullState cullState)
	{
		cullStateReq=cullState;
		return this;
	}
	/**
	 * Push the current state and clear all state to default.
	 */
	public void pushAndClear() {
		push();
		setLightEnabled(false);
		setCullState(ECullState.off);
		setTexture2d(false);
		setDepthTest(false);
		setBlendFunc(EBlendFunc.off);
	}
	/**
	 * Push the current state.
	 */
	public void push() {
		if(nSaves>=saves.size())
		{
			saves.add(new Save());
		}
		Save save=saves.get(nSaves);
		save.save(this);
		nSaves++;
	}
	/**
	 * Reset the state to the last pushed one.
	 */
	public void pop()
	{
		nSaves--;
		Save toReset=saves.get(nSaves);
		toReset.reset(this);
		apply(false);
	}
	public void setDepthTest(boolean b) {
		depthTestReq=b;
	}
	public void setTexture2d(boolean b) {
		texture2dReq=b;
	}
	public void setLightEnabled(boolean b) {
		lightEnabledReq=b;
	}
	public void apply()
	{
		apply(true);
	}
	public void apply(boolean force)
	{
		if(lightEnabledReq!=lightEnabled||force)
		{
			lightEnabled=lightEnabledReq;
			if(lightEnabled)
			{
				GL11.glEnable(GL11.GL_LIGHTING);
			}else
			{
				GL11.glDisable(GL11.GL_LIGHTING);
			}
		}
		if(texture2d!=texture2dReq||force)
		{
			texture2d=texture2dReq;
			if(texture2d)
			{
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			}else
			{
				GL11.glDisable(GL11.GL_TEXTURE_2D);
			}
		}
		if(!cullState.equals(cullStateReq)||force)
		{
			cullState=cullStateReq;
			switch (cullState) {
			case off:
				GL11.glDisable(GL11.GL_CULL_FACE);
				break;
			case back:
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glCullFace(GL11.GL_BACK);
				break;
			case front:
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glCullFace(GL11.GL_FRONT);
				break;
			default:
				break;
			}
		}
		if(depthTest!=depthTestReq||force)
		{
			depthTest=depthTestReq;
			if(depthTest)
			{
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}else
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			}
		}
		if(!blendFuncReq.equals(blendFunc)||force)
		{
			blendFunc=blendFuncReq;
			UtilGl.applyBlendFunc(blendFunc);
		}
	}
	public void setBlendFunc(EBlendFunc newBlendFunc) {
		blendFuncReq=newBlendFunc;
	}
	/**
	 * Get the default viewport of this GL context.
	 * @return The returned object is a copy of the stored one so it may be overwritten.
	 */
	public Rectangle getDefaultViewport() {
		return new Rectangle(defaultViewport);
	}
	public void setDefaultViewPort(Rectangle setViewPort) {
		defaultViewport=setViewPort;
	}
	public void resetViewport() {
		GL11.glViewport(defaultViewport.getX(), defaultViewport.getY(),
				defaultViewport.getWidth(), defaultViewport.getHeight());
	}
	IRenderOnTexture currentRenderOnTexture;
	public IRenderOnTexture getCurrentRenderOnTexture() {
		return currentRenderOnTexture;
	}
	public void setCurrentRenderOnTexture(IRenderOnTexture renderOnTexture) {
		currentRenderOnTexture=renderOnTexture;
	}
	public Rectangle getStoredViewPort() {
		return defaultViewport;
	}
}
