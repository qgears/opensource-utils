package hu.qgears.opengl.commons;

import hu.qgears.opengl.glut.GLContextProviderGlut;
import hu.qgears.opengl.lwjgl.GLContextProviderLwjgl;
import hu.qgears.opengl.mirgl.GlContextProviderMirGl;
import hu.qgears.opengl.osmesa.GlContextProviderOsMesaSdl;
import hu.qgears.opengl.osmesa.GlContextProviderOsMesaSwing;
import hu.qgears.opengl.osmesa.GlContextProviderOsMesaVncServer;
import hu.qgears.opengl.x11.GlContextProviderX11;

public enum EGLImplementation {
	lwjgl{
		@Override
		public IGlContextProvider createProvider() {
			return new GLContextProviderLwjgl();
		}
	},
	glut{
		@Override
			public IGlContextProvider createProvider() {
				return new GLContextProviderGlut();
			}
	},
	x11{
		@Override
		public IGlContextProvider createProvider() {
			return new GlContextProviderX11();
		}
	},
	mirgl{
		@Override
		public IGlContextProvider createProvider() {
			return new GlContextProviderMirGl();
		}
	},
	/**
	 * In-memory software rendering using OSMesa and show result in Swing window. 
	 */
	osmesaSwing{
		@Override
		public IGlContextProvider createProvider() {
			return new GlContextProviderOsMesaSwing();
		}
	},
	osmesaSdl{
		@Override
		public IGlContextProvider createProvider() {
			return new GlContextProviderOsMesaSdl();
		}
	},
	/**
	 * In memory software rendering with a VNC server started.
	 */
	osmesaVncServer{
		@Override
		public IGlContextProvider createProvider() {
			return new GlContextProviderOsMesaVncServer();
		}
	};
	public abstract IGlContextProvider createProvider();
}
