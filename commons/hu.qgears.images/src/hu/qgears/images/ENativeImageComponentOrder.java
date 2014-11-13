package hu.qgears.images;

/**
 * Pixel component orders of a native image in memory.
 * @author rizsi
 *
 */
public enum ENativeImageComponentOrder {
	RGB
	{
		@Override
		public int getNCHannels() {
			return 3;
		}
	},
	BGR	{
		@Override
		public int getNCHannels() {
			return 3;
		}
	},
	BGRA	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 3;
		}
	},
	RGBA	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 3;
		}
	},
	ARGB	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
	},
	ABGR	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
	},
	MONO	{
		@Override
		public int getNCHannels() {
			return 1;
		}
	},
	ALPHA	{
		@Override
		public int getNCHannels() {
			return 1;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
	};
	public int getNCHannels()
	{
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Index of the alpha channel
	 * @return -1 means no alpha channel
	 */
	public int getAlphaChannel() {
		return -1;
	}
}
