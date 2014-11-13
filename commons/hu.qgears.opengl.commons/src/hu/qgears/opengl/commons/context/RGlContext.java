package hu.qgears.opengl.commons.context;


import hu.qgears.opengl.commons.IRenderOnTexture;
import hu.qgears.opengl.commons.UtilGl;

import java.util.Stack;

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
final public class RGlContext implements Cloneable {
	Stack<RGlContext> stack=new Stack<RGlContext>();
	public void doSomething(){}
	ECullState cullState=ECullState.off;
	ECullState cullStateReq=ECullState.off;
	boolean lightEnabledReq;
	boolean lightEnabled;
	boolean texture2d, texture2dReq;
	boolean depthTestReq, depthTest;
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
		try {
			stack.push((RGlContext)this.clone());
		} catch (CloneNotSupportedException e) {
		}
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
		try {
			stack.push((RGlContext)this.clone());
		} catch (CloneNotSupportedException e) {
		}
	}
	/**
	 * Reset the state to the last pushed one.
	 */
	public void pop()
	{
		RGlContext toReset=stack.pop();
		lightEnabledReq=toReset.lightEnabled;
		texture2dReq=toReset.texture2d;
		cullStateReq=toReset.cullState;
		blendFuncReq=toReset.blendFunc;
		depthTestReq=toReset.depthTest;
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
	EBlendFunc blendFunc=EBlendFunc.off;
	EBlendFunc blendFuncReq=EBlendFunc.off;
	public void setBlendFunc(EBlendFunc newBlendFunc) {
		blendFuncReq=newBlendFunc;
	}
	Rectangle defaultViewport;
	public Rectangle getDefaultViewport() {
		return defaultViewport;
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
}
