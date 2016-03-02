package hu.qgears.opengl.commons;

import hu.qgears.opengl.glut.GLContextProviderGlut;
import hu.qgears.opengl.lwjgl.GLContextProviderLwjgl;
import hu.qgears.opengl.mirgl.GlContextProviderMirGl;
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
	};

	public abstract IGlContextProvider createProvider();
}
