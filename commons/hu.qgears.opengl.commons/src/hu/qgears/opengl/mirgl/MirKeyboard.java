package hu.qgears.opengl.mirgl;

import hu.qgears.opengl.x11.KeyboardImplX11;

/**
 * See MirKeyFlag
 * @author rizsi
 *
 */
public class MirKeyboard extends KeyboardImplX11
{
	@Override
	protected boolean isShift(int state) {
		return (state&1)!=0;
	}
	
	protected boolean isAlt(int state) {
		return (state&2)!=0;
	}
	@Override
	protected boolean isCtrl(int state) {
		return (state&0x1000)!=0;
	}
}
