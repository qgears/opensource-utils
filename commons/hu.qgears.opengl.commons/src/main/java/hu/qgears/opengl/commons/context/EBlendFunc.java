package hu.qgears.opengl.commons.context;

public enum EBlendFunc {
	off,
	SRC_ALPHA__ONE_MINUS_SRC_ALPHA,
	ALPHA,
	ALPHA_PREMULTIPLIED{
		@Override
		public boolean isPreMultiplied() {
			return true;
		}
	},;

	public boolean isPreMultiplied()
	{
		return false;
	}
}
